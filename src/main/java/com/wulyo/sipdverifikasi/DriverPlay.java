package com.wulyo.sipdverifikasi;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class DriverPlay extends Thread {
    private Browser browser;
    private Page page;
    private BrowserContext context;
    private boolean Stop;

    private DatabaseModel db;

    private DriverSignal driverSignal;

    private String username, password, tahun, provinsi;
    private String inputFile;

    private static final String INSERT_SQL = "INSERT INTO INFO_DATA (rowid, notbp, tanggal) VALUES (?, ?, ?)";
    public DriverPlay(DriverSignal driverSignal, DatabaseModel db, String username, String password, String tahun, String provinsi, String inputFile) {
        this.Stop = false;
        this.driverSignal = driverSignal;
        this.db = db;
        this.username = username;
        this.password = password;
        this.tahun = tahun;
        this.provinsi = provinsi;
        this.inputFile = inputFile;
    }

    public void setStop(boolean stop) {
        this.Stop = stop;

    }

    public BrowserContext getContext() { return this.context; }
    public Page getPage() { return this.page; }

    @Override
    public void run() {
        try (Playwright playwright = Playwright.create()) {
            driverSignal.onsetStatus("Mencoba buka browser....");
            this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(!db.getTanggalAsString()));
            this.context = this.browser.newContext();
            this.page = this.context.newPage();

            this.page.navigate("https://jatimprov.sipd.kemendagri.go.id/siap/login");

            driverSignal.onsetStatus("Mencoba pross login!!!");

            this.driverSignal.onBrowserOpen(); //do driver sudah kebuka dengan sempurna

            assertThat(page.locator("#email")).isEnabled();

            //melakukan isi
            page.locator("#email").fill(this.username);
            page.locator("#password").fill(this.password);
            page.locator("#tahunanggaran").selectOption(this.tahun);
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("--Pilih Wilayah Pemda--")).click();
            page.getByRole(AriaRole.SEARCHBOX).fill("jawa timur");


            page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(this.provinsi)).click();

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("LOGIN")).click();

            assertThat(page).hasURL(Pattern.compile(".*/home"));

            this.driverSignal.onsetStatus("Login done!");

            sleep(1000);

            this.driverSignal.onsetStatus("Memulai proses...");

            page.navigate(db.getLinkUrl());



            this.driverSignal.onsetStatus("Membaca file excel untuk eksekusi...");

            // ini untuk penggunaan database di memory
            try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:verifikasidb", "sa", "")) {
                // creta table

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE TABLE INFO_DATA(rowid varchar(100), notbp varchar(255), tanggal varchar(255))");
                }


                System.out.println(this.inputFile);

                FileInputStream file = new FileInputStream(this.inputFile);
                Workbook workbook = new XSSFWorkbook(file);

                Sheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());

                PreparedStatement stmt = conn.prepareStatement(INSERT_SQL);

                int startRow = Integer.parseInt(this.db.getstartRow());
                int maxRow = Integer.parseInt(this.db.getendRow());

                for (int i = startRow; i <= maxRow; i++) {
                    try {
                        if (this.isRowVisible(sheet, i, db.getColNoTbp())) {
                            var cellStatus = getCell(sheet, i, db.getColStatus());
                            var cellTbp = getCell(sheet, i, db.getColNoTbp());
                            var cellTanggal = getCell(sheet, i, db.getColTanggal());
                            var tanggal = "";
                            if (cellTanggal.getCellType() == CellType.NUMERIC) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                tanggal = dateFormat.format(cellTanggal.getDateCellValue());
                            } else {
                                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

                                tanggal = cellTanggal.getStringCellValue();
                                Date date = inputFormat.parse(tanggal);
                                tanggal = dateFormat.format(date);
                            }


                            if (isRowEmpty(cellStatus)) {
                                //hanya yang kosong saja
                                stmt.setString(1, String.valueOf(i));
                                stmt.setString(2, cellTbp.getStringCellValue());
                                stmt.setString(3, tanggal);

                                stmt.addBatch();

                            }


                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                //saving to database
                stmt.executeBatch();

                this.driverSignal.onsetStatus("File excel sudah terbaca, melakukan proses update data!");
                sleep(1000);

                // melakukan proses input

                assertThat(page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Pembuatan LPJ"))).isVisible();

                page.getByRole (AriaRole.TAB, new Page.GetByRoleOptions().setName("Pembuatan LPJ")).click();

                var elemData = page.locator("tr[ng-repeat=\"(i, TBP) in listTbpForSpj  | filter:searchlistTbp\"]");

                var i = 0;
                while (elemData.count() > 0) {
                    if (Stop) break;
                    this.driverSignal.onsetStatus("Menyiapkan data tbp ...");
                    var td = elemData.all().get(i).locator("td").all();

                    var tbp = td.get(1).innerText().trim();

                    Statement stmtCari = conn.createStatement();
                    this.driverSignal.onsetStatus("Cari data " + tbp + " .... ");
                    ResultSet rs = stmtCari.executeQuery("SELECT * FROM INFO_DATA WHERE notbp = '" + tbp +"' LIMIT 1");

                    if (rs.next()) {
                        String rowId = rs.getString("rowid");
                        String tbpStmt = rs.getString("notbp");
                        String tanggal = rs.getString("tanggal");

                        String[] newData = {rowId, tbpStmt, tanggal};
                        this.driverSignal.onAddTable(newData);

                        //lakukan proses saving
                        try {
                            td.get(4).locator("button").click();
                            var pageBar = page.locator("div.input-spj");
                            assertThat(pageBar).isInViewport();

                            var noLpj = pageBar.locator("//input[@ng-model='formTambah.nomorSpj']");
                            assertThat(noLpj).hasText(Pattern.compile(".*"));

                            var tglLpj = pageBar.locator("input[ng-model='formTambah.tanggalSpj']");
                            tglLpj.fill(tanggal);
                            tglLpj.press("Tab");

                            sleep(1000);

                            this.driverSignal.onsetStatus("Simpan tbp = " + tbp);
                            pageBar.locator("#btnSubmitSpj").click();

                            sleep(1000);
                            this.driverSignal.onsetStatus("Ok");
                            this.driverSignal.onUpdateTable("ok.");
                            var cellku = getCell(sheet, Integer.parseInt(rowId), db.getColStatus());
                            if (cellku != null)
                                cellku.setCellValue("ok");


                        } catch (Exception ex) {
                            this.driverSignal.onsetStatus(ex.getMessage());
                            this.driverSignal.onUpdateTable(ex.getMessage());
                            var cellku = getCell(sheet, Integer.parseInt(rowId), db.getColStatus());
                            if (cellku != null)
                                cellku.setCellValue(ex.getMessage());
                            i = i + 1; //loncati dia
                        }
                    } else {
                        String[] newData = {"x", tbp, "-", "no-data"};
                        this.driverSignal.onAddTable(newData);
                    }


                    assertThat(page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Pembuatan LPJ"))).isVisible();
                    page.getByRole (AriaRole.TAB, new Page.GetByRoleOptions().setName("Pembuatan LPJ")).click();
                    elemData = page.locator("tr[ng-repeat=\"(i, TBP) in listTbpForSpj  | filter:searchlistTbp\"]");

                }

                // lakukan saving
                try (FileOutputStream fileOut = new FileOutputStream(this.inputFile)) {
                    workbook.write(fileOut);

                } catch (IOException e) {
                    FileOutputStream fileOut = new FileOutputStream(this.inputFile + "-baru.xlsx");
                    workbook.write(fileOut);
                }

                workbook.close();
                file.close();
            }



            this.driverSignal.onsetStatus("All done!");
            // listing all datas

            this.context.close();
            this.browser.close();
            this.driverSignal.onDone(); //do tell gui kalau pekerjaan sudah selesai gaes
        } catch (Exception t) {
            t.printStackTrace();
        }
    }

    private Cell getCell(Sheet sheet, int row, String abc) {
        CellReference cellReference = new CellReference(abc + String.valueOf(row));
        Row numRow = sheet.getRow(cellReference.getRow());
        if (numRow != null) {
            return numRow.getCell(cellReference.getCol());
        }
        return null;
    }

    private boolean isRowVisible(Sheet sheet, int row, String abc) {
        CellReference cellReference = new CellReference(abc + String.valueOf(row));
        Row numRow = sheet.getRow(cellReference.getRow());
        CellStyle rowStyle = numRow.getRowStyle();
        return rowStyle == null || rowStyle.getHidden();
    }

    private boolean isRowEmpty(Cell cell) {
        if (cell == null) return false;
        return cell.getCellType() == CellType.BLANK || cell.getStringCellValue().trim().isEmpty();
    }

}
