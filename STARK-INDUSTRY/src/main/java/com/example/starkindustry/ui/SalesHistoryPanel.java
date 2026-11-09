package com.example.starkindustry.ui;

import com.example.starkindustry.dao.SalesHistoryDAO;
import com.example.starkindustry.model.User;
import com.example.starkindustry.util.ReportUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

/** Search past sales, print invoices and process audited customer returns. */
public class SalesHistoryPanel extends JPanel {
    private final SalesHistoryDAO dao=new SalesHistoryDAO();
    private final User user;
    private final JTextField search=new JTextField(18);
    private final DefaultTableModel invoices=new DefaultTableModel(
        new String[]{"Invoice","Date","Customer","Original (Rs.)","Payment","Refunded (Rs.)","Net (Rs.)"},0){
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final DefaultTableModel lines=new DefaultTableModel(
        new String[]{"Item ID","Part No","Part","Sold","Unit Price","Returned","Remaining","Status"},0){
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final DefaultTableModel returns=new DefaultTableModel(
        new String[]{"Return ID","Date","Part No","Qty","Refund (Rs.)","Reason"},0){
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable invoiceTable=new JTable(invoices),itemTable=new JTable(lines);
    private final JTable returnTable=new JTable(returns);
    private final JLabel info=new JLabel("Select an invoice to view items and returns.");

    public SalesHistoryPanel(User user){
        this.user=user;setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(15,20,15,20));
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel title=new JLabel("Sales History & Returns");
        title.setFont(title.getFont().deriveFont(Font.BOLD,21f));
        JButton find=new JButton("Search / Refresh"),print=new JButton("Print Invoice"),refund=new JButton("Return Selected Item");
        top.add(title);top.add(new JLabel("Search"));top.add(search);top.add(find);top.add(print);top.add(refund);
        add(top,BorderLayout.NORTH);
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceTable.setAutoCreateRowSorter(true);
        JPanel bottom=new JPanel(new BorderLayout(4,4));
        JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("Invoice Items",new JScrollPane(itemTable));
        tabs.addTab("Returns History",new JScrollPane(returnTable));
        bottom.add(info,BorderLayout.NORTH);bottom.add(tabs,BorderLayout.CENTER);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(invoiceTable),bottom);
        split.setResizeWeight(.55);add(split,BorderLayout.CENTER);
        find.addActionListener(e->refresh());
        search.addActionListener(e->refresh());
        invoiceTable.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())loadSelected();});
        print.addActionListener(e->{int id=selectedInvoice();if(id>0)try{ReportUtil.showInvoice(id);}catch(RuntimeException ex){error(ex.getMessage());}});
        refund.addActionListener(e->returnSelected());
    }
    public void refresh(){
        try{
            invoices.setRowCount(0);lines.setRowCount(0);returns.setRowCount(0);
            for(Object[] r:dao.history(search.getText().trim()))invoices.addRow(r);
            info.setText(invoices.getRowCount()+" invoices found.");
        }catch(RuntimeException ex){error(ex.getMessage());}
    }
    private int selectedInvoice(){
        int row=invoiceTable.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select an invoice first.");return -1;}
        return (Integer)invoices.getValueAt(invoiceTable.convertRowIndexToModel(row),0);
    }
    private void loadSelected(){
        int row=invoiceTable.getSelectedRow();if(row<0)return;
        int id=(Integer)invoices.getValueAt(invoiceTable.convertRowIndexToModel(row),0);
        try{
            lines.setRowCount(0);returns.setRowCount(0);
            for(Object[] r:dao.items(id)){
                int sold=(Integer)r[3], returned=(Integer)r[5], remaining=sold-returned;
                String status=remaining==0?"Fully Returned":returned>0?"Partially Returned":"Not Returned";
                lines.addRow(new Object[]{r[0],r[1],r[2],sold,r[4],returned,remaining,status});
            }
            for(Object[] r:dao.returns(id))returns.addRow(r);
            info.setText("Invoice #"+id+" | "+lines.getRowCount()+" lines | "+returns.getRowCount()+" returns");
        }catch(RuntimeException ex){error(ex.getMessage());}
    }
    private void returnSelected(){
        int row=invoiceTable.getSelectedRow();
        if(row<0){error("Select an invoice first.");return;}
        int invoiceId=(Integer)invoices.getValueAt(invoiceTable.convertRowIndexToModel(row),0);
        int itemRow=itemTable.getSelectedRow();
        if(itemRow<0){error("Select an item under Invoice Items first.");return;}
        int modelRow=itemTable.convertRowIndexToModel(itemRow);
        int itemId=(Integer)lines.getValueAt(modelRow,0);
        int available=(Integer)lines.getValueAt(modelRow,6);
        if(available<=0){error("This item is marked Fully Returned. No additional returns are allowed.");return;}
        String input=JOptionPane.showInputDialog(this,"Quantity to return (maximum "+available+")","1");
        if(input==null)return;
        int qty;
        try{qty=Integer.parseInt(input.trim());if(qty<=0||qty>available)throw new NumberFormatException();}
        catch(NumberFormatException ex){error("Enter a quantity between 1 and "+available+".");return;}
        String reason=JOptionPane.showInputDialog(this,"Reason for return:");
        if(reason==null)return;
        if(reason.isBlank()||reason.length()>255){error("A reason (maximum 255 characters) is required.");return;}
        if(JOptionPane.showConfirmDialog(this,"Return "+qty+" unit(s)? Stock will increase and the refund will be recorded.",
            "Confirm Return",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{
            BigDecimal amount=dao.returnItem(invoiceId,itemId,qty,reason,user.getUserId());
            JOptionPane.showMessageDialog(this,"Return recorded. Refund: Rs. "+amount);
            refresh();
            for(int i=0;i<invoices.getRowCount();i++)if((Integer)invoices.getValueAt(i,0)==invoiceId){
                int view=invoiceTable.convertRowIndexToView(i);invoiceTable.setRowSelectionInterval(view,view);break;
            }
        }catch(RuntimeException ex){error(ex.getMessage());}
    }
    private void error(String msg){JOptionPane.showMessageDialog(this,msg,"Sales History",JOptionPane.WARNING_MESSAGE);}
}
