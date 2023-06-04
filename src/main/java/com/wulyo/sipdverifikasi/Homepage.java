package com.wulyo.sipdverifikasi;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import org.apache.poi.ss.formula.functions.T;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class Homepage extends JFrame {
    private JPanel HomePanel;
    private JPanel Logo;
    private JPanel InputForm;
    private JTextField txtFile;
    private JPanel fileinput;
    private JButton button1;
    private JPanel start;
    private JLabel lblStatus;
    private JTable table1;
    private JButton loginButton;
    private JButton stopButton;
    private JButton startButton;
    private JTextField txtStart;
    private JTextField txtMax;
    private JCheckBox chkTglAsString;
    private JTextField txtStatus;
    private JTextField txtNoTbp;
    private JTextField txtTanggal;
    private JTextField txtUser;
    private JTextField txtPass;
    private JTextField txtTahun;
    private JTextField txtProvinsi;
    DefaultTableModel TableModel;

    DatabaseModel dbModel;

    DriverPlay driverPlay;
    DriverSignal driverSignal;

    public Homepage() {
        setContentPane(HomePanel);
        setTitle("SIPD Verifikasi");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(670,800);
        setLocationRelativeTo(null);
        setResizable(false);
        // set icon
        ImageIcon icon = new ImageIcon(getClass().getResource("/icons/app-icon.png"));
        setIconImage(icon.getImage());
        setVisible(true);

        this.initTable();
        this.initData();

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                var filter = new FileNameExtensionFilter("Excel file", "xlsx");
                fileChooser.addChoosableFileFilter(filter);
                fileChooser.setFileFilter(filter);
                int result = fileChooser.showOpenDialog(HomePanel);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String filePath = selectedFile.getAbsolutePath();
                    txtFile.setText(filePath);
                    dbModel.setFilePath(filePath);
                    DatabaseModel.saveConfig(dbModel, DatabaseModel.configPath);

                }

            }
        });
        txtStart.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                dbModel.setStartRow(txtStart.getText());
                DatabaseModel.saveConfig(dbModel, DatabaseModel.configPath);
            }
        });
        txtMax.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                dbModel.setEndRow(txtMax.getText());
                DatabaseModel.saveConfig(dbModel, DatabaseModel.configPath);
            }
        });

        txtStatus.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                dbModel.setColStatus(txtStatus.getText());
                DatabaseModel.saveConfig(dbModel, DatabaseModel.configPath);
            }
        });
        txtNoTbp.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                dbModel.setColNoTbp(txtNoTbp.getText());
                DatabaseModel.saveConfig(dbModel, DatabaseModel.configPath);
            }
        });
        txtTanggal.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                dbModel.setColTanggal(txtTanggal.getText());
                DatabaseModel.saveConfig(dbModel, DatabaseModel.configPath);
            }
        });
        chkTglAsString.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                dbModel.setTanggalAsString(chkTglAsString.isSelected());
                DatabaseModel.saveConfig(dbModel, DatabaseModel.configPath);
            }
        });
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                driverSignal = new DriverSignal() {
                    @Override
                    public void onBrowserOpen() {
                        // do something
                        startButton.setEnabled(true);
                        stopButton.setEnabled(true);

                    }

                    @Override
                    public void onDone() {
                        loginButton.setEnabled(true);
                        startButton.setEnabled(false);
                        stopButton.setEnabled(false);
                    }

                    @Override
                    public void onsetStatus(String status) {
                        lblStatus.setText(status);
                    }

                    @Override
                    public void onAddTable(Object[] rowData) {
                        TableModel.addRow(rowData);
                    }

                    @Override
                    public void onUpdateTable(String status) {
                        TableModel.setValueAt(status, TableModel.getRowCount() - 1, 3);
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println(error);
                    }

                };
                TableModel.setRowCount(0);
                lblStatus.setText("Installing driver ...");
                loginButton.setEnabled(false);
                driverPlay = new DriverPlay(driverSignal, dbModel, txtUser.getText(), txtPass.getText(), txtTahun.getText(), txtProvinsi.getText(), txtFile.getText());
                driverPlay.start();
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                driverPlay.setStop(true);

            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                startButton.setEnabled(false);
                //VerifikasiGu vg = new VerifikasiGu(driverPlay.getPage(), driverPlay.getContext(), dbModel, driverSignal);
                //vg.start();



            }
        });
    }

    public void initTable() {
        TableModel = new DefaultTableModel();
        Object[] colName = {"Row", "No TBP", "Tanggal", "Status"};
        TableModel.setColumnIdentifiers(colName);


        table1.setModel(TableModel);
        table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel tableColumnModel = table1.getColumnModel();
        tableColumnModel.getColumn(0).setPreferredWidth(60);
        tableColumnModel.getColumn(1).setPreferredWidth(250);
        tableColumnModel.getColumn(2).setPreferredWidth(90);
        tableColumnModel.getColumn(3).setPreferredWidth(170);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        tableColumnModel.getColumn(1).setCellRenderer(renderer);
        tableColumnModel.getColumn(2).setCellRenderer(renderer);

        table1.setShowGrid(true);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
    }

    public void initData() {
        File file = new File(DatabaseModel.configPath);
        if (file.exists()) {
            dbModel = DatabaseModel.readConfig(DatabaseModel.configPath);
            if (dbModel != null) {
                txtFile.setText(dbModel.getFilePath());
                txtStart.setText(dbModel.getstartRow());
                txtMax.setText(dbModel.getendRow());
                txtStatus.setText(dbModel.getColStatus());
                txtTanggal.setText(dbModel.getColTanggal());
                txtNoTbp.setText(dbModel.getColNoTbp());
                chkTglAsString.setSelected(dbModel.getTanggalAsString());
            }else {
                dbModel = new DatabaseModel();
            }

        } else {
            dbModel = new DatabaseModel();
        }

    }

}
