/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package projectbasdat.Customer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import projectbasdat.DatabaseTools;

/**
 *
 * @author tridi
 */
public class MainCustomer extends javax.swing.JFrame {
    DatabaseTools db = new DatabaseTools();
    ArrayList<String> daftarOrderId, daftarOrderIdAktif;
    String email,password;
    int id_pelanggan;
    
    /**
     * Creates new form Main
     */
    public MainCustomer() {
        initComponents();
        refresh();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                db.close();
            }
        });
    }
    public MainCustomer(String email, String password) {
        initComponents();
        populateProfil(email, password);
        this.email = email;
        this.password = password;
        initId_Pelanggan();
        refresh();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                db.close();
            }
        });
        
        //listener untuk tabel order
        ListSelectionModel modelDetail = tableOrder.getSelectionModel();
        modelDetail.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!modelDetail.isSelectionEmpty()) {
                    int index = modelDetail.getMinSelectionIndex();
                    String id = daftarOrderId.get(index);
                    populateTableDetailPesanan(id);
                }
            }
        });
        
        //listener untuk tabel order aktif
        ListSelectionModel modelDetail1 = TableOrderAktif.getSelectionModel();
        modelDetail1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!modelDetail1.isSelectionEmpty()) {
                    int index = modelDetail1.getMinSelectionIndex();
                    String id = daftarOrderIdAktif.get(index);
                    populateTableDetailPesananAktif(id);
                }
            }
        });
        
    }
    
    private void initId_Pelanggan(){
        try {
            String query = String.format("select id_pelanggan from customer_account where email = %s",email);
            System.out.println(query);
            ResultSet rs = db.runQuery(query);
            while (rs.next()){
                id_pelanggan = Integer.parseInt(rs.getString("id_pelanggan"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MainCustomer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getEmail(){
        return email;
    }
    
    private void populateTableOrder(){
        try {
            String query = String.format("exec get_riwayatPesanan %s",id_pelanggan);
            System.out.println(query);
            ResultSet rs = db.runQuery(query);
            
            DefaultTableModel tableModel = (DefaultTableModel)tableOrder.getModel();
            tableModel.setRowCount(0);
            daftarOrderId = new ArrayList();
            int count = 1;
            String status;
            while (rs.next()) {
                daftarOrderId.add(rs.getString("order_id"));
                if(rs.getString("status_order").equals("1")){
                    status = "Terkirim";
                }else{
                    status = "Sedang dikirim";
                }
                tableModel.addRow(new Object[] {
                    count,
                    rs.getString("tanggal_kirim"),
                    status
                });
                count++;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "populateTableOrder error");
        }
    }
    
    private void populateTableDetailPesanan(String order_id) {
        try {
            String query = String.format("exec get_detailpesanan %s", order_id);
            System.out.println(query);
            ResultSet rs = db.runQuery(query);
            DefaultTableModel tableModel = (DefaultTableModel)TableDetailPesanan.getModel();
            tableModel.setRowCount(0);
            int total = 0;
            int temp;
            while (rs.next()) {
                temp = Integer.parseInt(rs.getString("subtotal"));
                total += temp;
                tableModel.addRow(new Object[] {
                    rs.getString("nama_produk"),
                    rs.getString("kategori"),
                    rs.getString("kuantitas"),
                    rs.getString("harga_satuan"),
                    rs.getString("subtotal")
                });
            }
            totalText.setText(String.valueOf(total));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "populateTableDetailPesanan error");
        }
    }
    
    private void pupulateTableOrderAktif(){
        try {
            String query = String.format("exec get_pesananaktif %s",id_pelanggan);
            System.out.println(query);
            ResultSet rs = db.runQuery(query);
            
            DefaultTableModel tableModel = (DefaultTableModel)TableOrderAktif.getModel();
            tableModel.setRowCount(0);
            daftarOrderIdAktif = new ArrayList();
            int count = 1;
            while (rs.next()) {
                daftarOrderIdAktif.add(rs.getString("order_id"));               
                tableModel.addRow(new Object[] {
                    count,
                    rs.getString("tanggal_kirim")
                });
                count++;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "pupulateTableOrderAktif error");
        }
    }
    
    private void populateTableDetailPesananAktif(String order_id) {
        try {
            String query = String.format("exec get_detailpesanan %s", order_id);
            System.out.println(query);
            ResultSet rs = db.runQuery(query);
            DefaultTableModel tableModel = (DefaultTableModel)TableDetailPesananAktif.getModel();
            tableModel.setRowCount(0);
            int total = 0;
            int temp;
            while (rs.next()) {
                temp = Integer.parseInt(rs.getString("subtotal"));
                total += temp;
                tableModel.addRow(new Object[] {
                    rs.getString("nama_produk"),
                    rs.getString("kategori"),
                    rs.getString("kuantitas"),
                    rs.getString("harga_satuan"),
                    rs.getString("subtotal")
                });
            }
            TotalTextAktif.setText(String.valueOf(total));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "populateTableDetailPesananAktif error");
        }
    }
    
    
    private void populateProfil(String email, String password){
        String query = String.format("exec get_profil_pelanggan %s, %s", email, password);
//        String query=String.format("select * from customer_account ca join customer_profile cp "
//                + "on ca.id_pelanggan=cp.id_pelanggan "
//                + " where email= %s and password= %s",e,p);
        ResultSet rs;
        try {
            rs = db.runQuery(query);
            while(rs.next()){
            textUsername.setText(rs.getString("nama_pelanggan"));
            textEmail.setText(rs.getString("email"));
            textPassword.setText(rs.getString("password"));
            textNama.setText(rs.getString("nama_pelanggan"));
            comboboxJenisKelamin.setSelectedItem(rs.getString("jenis_kelamin"));
            textTanggalLahir.setText(rs.getString("tanggal_lahir"));
            textNomorHp.setText(rs.getString("nomor_hp"));
            textNomorRumah.setText(rs.getString("nomor_rumah"));
            textDesaKecamatan.setText(rs.getString("desa_kecamatan"));
            textKabupatenKota.setText(rs.getString("kabupaten_kota"));
            textJalan.setText(rs.getString("jalan"));
            textKodePos.setText(rs.getString("kode_pos"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MainCustomer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updateProfil(){;
        String nama="'"+textNama.getText()+"'";
        String email="'"+textEmail.getText()+"'";
        String password="'"+textPassword.getText()+"'";
        String jk="'"+(String)comboboxJenisKelamin.getSelectedItem()+"'";
        String tgl="'"+textTanggalLahir.getText()+"'";
        String nomorHp=textNomorHp.getText();
        String nomorRumah="'"+textNomorRumah.getText()+"'";
        String desaKec="'"+textDesaKecamatan.getText()+"'";
        String kabKota="'"+textKabupatenKota.getText()+"'";
        String jalan="'"+textJalan.getText()+"'";
        String kodePos="'"+textKodePos.getText()+"'";
        
        String query = String.format("exec get_profil_pelanggan %s, %s", email, password);
        ResultSet rs;
        try{
            rs = db.runQuery(query);
            rs.next();
            String id=rs.getString("id_pelanggan");
            String query2=String.format("exec update_profil_pelanggan %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
                id, nama, tgl, nomorHp, nomorRumah, desaKec, kabKota, jalan, jk, kodePos);
            String query3=String.format("exec update_akun_pelanggan %s, %s, %s", id, email, password);
            
            System.out.println(id);
            System.out.println(query2);
            System.out.println(query3);
            db.runUpdateQuery(query2);
            db.runUpdateQuery(query3);
            JOptionPane.showMessageDialog(null, "Berhasil");
            
        }catch(SQLException ex){
            Logger.getLogger(MainCustomer.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Gagal mengupdate");
        }
        populateProfil(email, password);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        textUsername = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        textNomorRumah = new javax.swing.JTextField();
        textTanggalLahir = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        textKodePos = new javax.swing.JTextField();
        textDesaKecamatan = new javax.swing.JTextField();
        textKabupatenKota = new javax.swing.JTextField();
        textNama = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        textNomorHp = new javax.swing.JTextField();
        textPassword = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        textEmail = new javax.swing.JTextField();
        textJalan = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        comboboxJenisKelamin = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableOrder = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        TableDetailPesanan = new javax.swing.JTable();
        totalText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        TableOrderAktif = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        TableDetailPesananAktif = new javax.swing.JTable();
        TotalTextAktif = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable7 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jTextField6 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Nama Anda");

        textUsername.setEditable(false);

        jLabel9.setText("Desa/Kecamatan");

        jLabel12.setText("Jenis Kelamin");

        jLabel16.setText("Password");

        jLabel13.setText("Kode Pos");

        jButton2.setText("Edit Data");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel5.setText("Nama");

        jLabel11.setText("Jalan");

        jLabel6.setText("Tanggal Lahir");

        jLabel15.setText("Email");

        jLabel7.setText("Nomor HP");

        jLabel10.setText("Kabupaten/Kota");

        jLabel8.setText("Nomor Rumah");

        comboboxJenisKelamin.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "L", "P" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(389, 389, 389)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel12)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)
                            .addComponent(jLabel13)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(textKodePos)
                            .addComponent(textJalan)
                            .addComponent(textKabupatenKota)
                            .addComponent(textDesaKecamatan)
                            .addComponent(textNomorRumah)
                            .addComponent(textNama)
                            .addComponent(textTanggalLahir)
                            .addComponent(textNomorHp)
                            .addComponent(textEmail)
                            .addComponent(textPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboboxJenisKelamin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(104, 104, 104)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(457, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(99, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(textEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(textPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(textNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(comboboxJenisKelamin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(textTanggalLahir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(textNomorHp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(textNomorRumah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(textDesaKecamatan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(textKabupatenKota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(textJalan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(textKodePos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addComponent(jButton2)
                .addGap(51, 51, 51))
        );

        jTabbedPane1.addTab("Profil", jPanel1);

        tableOrder.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "nomor", "tanggal kirim", "status order"
            }
        ));
        jScrollPane1.setViewportView(tableOrder);

        TableDetailPesanan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nama Produk", "Kategori", "Kuantitas", "Harga", "Subtotal"
            }
        ));
        jScrollPane2.setViewportView(TableDetailPesanan);

        totalText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                totalTextActionPerformed(evt);
            }
        });

        jLabel3.setText("Total");

        jButton4.setText("Print Riwayat Order");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1157, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 565, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 518, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(totalText))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGap(145, 145, 145)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(4, 4, 4)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 608, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(56, 56, 56)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(totalText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3))))
                    .addGap(18, 18, 18)
                    .addComponent(jButton4)
                    .addContainerGap(56, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Riwayat Pesanan", jPanel2);

        TableOrderAktif.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "nomor", "tanggal kirim"
            }
        ));
        jScrollPane3.setViewportView(TableOrderAktif);

        TableDetailPesananAktif.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nama Produk", "Kategori", "Kuantitas", "Harga", "Subtotal"
            }
        ));
        jScrollPane4.setViewportView(TableDetailPesananAktif);

        jLabel4.setText("Total");

        jButton5.setText("Print Data Pesanan");

        jButton6.setText("Batalkan Pesanan");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 565, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(142, 142, 142)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 518, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(TotalTextAktif))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(145, 145, 145)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(4, 4, 4))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TotalTextAktif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton5)
                    .addComponent(jButton6))
                .addContainerGap(56, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Pesanan Aktif", jPanel4);

        jTable7.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nama Produk", "Jumlah Stok", "Harga", "Kategori"
            }
        ));
        jScrollPane7.setViewportView(jTable7);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Pilih Produk");

        jButton1.setText("Tambah Produk");

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nama Produk", "Jumlah", "Harga", "Subtotal"
            }
        ));
        jScrollPane5.setViewportView(jTable3);

        jTextField6.setEditable(false);

        jLabel14.setText("Jumlah");

        jLabel17.setText("Total");

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Keranjang");

        jButton3.setText("Buat Pesanan");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(9, 9, 9))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 436, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField7)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3)))
        );

        jTabbedPane1.addTab("Buat Pesanan", jPanel3);

        jButton7.setText("Keluar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1157, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(textUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7))
                .addGap(31, 31, 31)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 641, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        updateProfil();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void totalTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_totalTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_totalTextActionPerformed
    
    private void refresh(){
        populateTableOrder();
        pupulateTableOrderAktif();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainCustomer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainCustomer().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable TableDetailPesanan;
    private javax.swing.JTable TableDetailPesananAktif;
    private javax.swing.JTable TableOrderAktif;
    private javax.swing.JTextField TotalTextAktif;
    private javax.swing.JComboBox<String> comboboxJenisKelamin;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable7;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTable tableOrder;
    private javax.swing.JTextField textDesaKecamatan;
    private javax.swing.JTextField textEmail;
    private javax.swing.JTextField textJalan;
    private javax.swing.JTextField textKabupatenKota;
    private javax.swing.JTextField textKodePos;
    private javax.swing.JTextField textNama;
    private javax.swing.JTextField textNomorHp;
    private javax.swing.JTextField textNomorRumah;
    private javax.swing.JTextField textPassword;
    private javax.swing.JTextField textTanggalLahir;
    private javax.swing.JTextField textUsername;
    private javax.swing.JTextField totalText;
    // End of variables declaration//GEN-END:variables
}
