package com.example.starkindustry.dao;

import com.example.starkindustry.config.DBConnection;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Read-only invoice history and transactional customer returns. */
public class SalesHistoryDAO {
    public List<Object[]> history(String keyword) {
        String sql = "SELECT s.invoice_id,s.invoice_date,c.name,s.total_amount,s.payment_method," +
            "COALESCE((SELECT SUM(r.refund_amount) FROM sales_returns r WHERE r.invoice_id=s.invoice_id),0) AS refunded " +
            "FROM sales_invoices s JOIN customers c ON c.customer_id=s.customer_id " +
            "WHERE CAST(s.invoice_id AS CHAR) LIKE ? OR c.name LIKE ? ORDER BY s.invoice_id DESC";
        List<Object[]> rows=new ArrayList<>();
        try (PreparedStatement ps=DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            String q="%"+keyword+"%"; ps.setString(1,q); ps.setString(2,q);
            try(ResultSet rs=ps.executeQuery()) { while(rs.next()) {
                BigDecimal total=rs.getBigDecimal("total_amount"), refunded=rs.getBigDecimal("refunded");
                rows.add(new Object[]{rs.getInt("invoice_id"),rs.getDate("invoice_date"),rs.getString("name"),
                    total,rs.getString("payment_method"),refunded,total.subtract(refunded)});
            }}
        } catch(SQLException e) {throw new IllegalStateException("Could not load sales history. Have you run the returns migration?",e);}
        return rows;
    }

    public List<Object[]> items(int invoiceId) {
        List<Object[]> rows=new ArrayList<>();
        String sql="SELECT i.item_id,p.part_no,p.name,i.quantity,i.unit_price,"+
            "COALESCE((SELECT SUM(r.quantity) FROM sales_returns r WHERE r.item_id=i.item_id),0) AS returned "+
            "FROM invoice_items i JOIN parts p ON p.part_id=i.part_id WHERE i.invoice_id=? ORDER BY i.item_id";
        try(PreparedStatement ps=DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1,invoiceId);
            try(ResultSet rs=ps.executeQuery()){while(rs.next())rows.add(new Object[]{
                rs.getInt("item_id"),rs.getString("part_no"),rs.getString("name"),
                rs.getInt("quantity"),rs.getBigDecimal("unit_price"),rs.getInt("returned")});}
        }catch(SQLException e){throw new IllegalStateException("Could not load invoice items.",e);}
        return rows;
    }

    public List<Object[]> returns(int invoiceId) {
        List<Object[]> rows=new ArrayList<>();
        String sql="SELECT r.return_id,r.returned_at,p.part_no,r.quantity,r.refund_amount,r.reason "+
            "FROM sales_returns r JOIN parts p ON p.part_id=r.part_id WHERE r.invoice_id=? ORDER BY r.return_id DESC";
        try(PreparedStatement ps=DBConnection.getInstance().getConnection().prepareStatement(sql)){
            ps.setInt(1,invoiceId);
            try(ResultSet rs=ps.executeQuery()){while(rs.next())rows.add(new Object[]{
                rs.getInt(1),rs.getTimestamp(2),rs.getString(3),rs.getInt(4),rs.getBigDecimal(5),rs.getString(6)});}
        }catch(SQLException e){throw new IllegalStateException("Could not load returns.",e);}
        return rows;
    }

    /** Return one invoice line in a single ACID transaction. Partial returns allowed. */
    public BigDecimal returnItem(int invoiceId,int itemId,int quantity,String reason,int userId) {
        if(invoiceId<=0||itemId<=0||quantity<=0||userId<=0||reason==null||reason.isBlank()||reason.length()>255)
            throw new IllegalArgumentException("Select an invoice item, positive quantity and a return reason.");
        Connection con=DBConnection.getInstance().getConnection();
        try {
            con.setAutoCommit(false);
            int partId, sold, previouslyReturned;
            BigDecimal unitPrice,subtotal,discount;
            String sql="SELECT i.part_id,i.quantity,i.unit_price,s.sub_total,s.discount FROM invoice_items i "+
                "JOIN sales_invoices s ON s.invoice_id=i.invoice_id WHERE i.item_id=? AND i.invoice_id=? FOR UPDATE";
            try(PreparedStatement ps=con.prepareStatement(sql)){
                ps.setInt(1,itemId);ps.setInt(2,invoiceId);
                try(ResultSet rs=ps.executeQuery()){
                    if(!rs.next())throw new IllegalArgumentException("Invoice item not found.");
                    partId=rs.getInt(1);sold=rs.getInt(2);unitPrice=rs.getBigDecimal(3);
                    subtotal=rs.getBigDecimal(4);discount=rs.getBigDecimal(5);
                }
            }
            try(PreparedStatement ps=con.prepareStatement("SELECT COALESCE(SUM(quantity),0) FROM sales_returns WHERE item_id=?")){
                ps.setInt(1,itemId);try(ResultSet rs=ps.executeQuery()){rs.next();previouslyReturned=rs.getInt(1);}
            }
            if(quantity>sold-previouslyReturned)throw new IllegalArgumentException("Return exceeds remaining sold quantity.");
            // Pro-rate the original invoice-level discount; do not refund more than the customer paid.
            BigDecimal ratio=subtotal.signum()==0?BigDecimal.ONE:
                subtotal.subtract(discount).divide(subtotal,10,RoundingMode.HALF_UP);
            BigDecimal refund=unitPrice.multiply(BigDecimal.valueOf(quantity)).multiply(ratio)
                .setScale(2,RoundingMode.HALF_UP);
            try(PreparedStatement ps=con.prepareStatement("INSERT INTO sales_returns(invoice_id,item_id,part_id,quantity,refund_amount,reason,processed_by) VALUES(?,?,?,?,?,?,?)")){
                ps.setInt(1,invoiceId);ps.setInt(2,itemId);ps.setInt(3,partId);ps.setInt(4,quantity);
                ps.setBigDecimal(5,refund);ps.setString(6,reason.trim());ps.setInt(7,userId);ps.executeUpdate();
            }
            try(PreparedStatement ps=con.prepareStatement("UPDATE parts SET stock_qty=stock_qty+? WHERE part_id=?")){
                ps.setInt(1,quantity);ps.setInt(2,partId);
                if(ps.executeUpdate()!=1)throw new SQLException("Part not found");
            }
            con.commit();return refund;
        }catch(SQLException|RuntimeException e){
            try{con.rollback();}catch(SQLException ignored){}
            if(e instanceof RuntimeException)throw (RuntimeException)e;
            throw new IllegalStateException("Return failed. No changes were saved.",e);
        }finally{try{con.setAutoCommit(true);}catch(SQLException ignored){}}
    }
}
