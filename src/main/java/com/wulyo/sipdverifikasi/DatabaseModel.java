package com.wulyo.sipdverifikasi;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import com.google.gson.Gson;

public class DatabaseModel {
    private String FilePath;
    private String startRow;
    private String endRow;
    private String colStatus;
    private String colTanggal;
    private String colNoTbp;
    private boolean tanggalAsString;
    private String linkUrl = "https://jatimprov.sipd.kemendagri.go.id/siap/spj";
    public static String configPath = Paths.get(System.getProperty("java.io.tmpdir"), "hasna-sakha-config-verifikasi.json").toString();

    public DatabaseModel() {

    }
    public DatabaseModel(String FilePath, String startRow, String endRow,
                         String colStatus, String colTanggal, String colNoTbp, boolean tanggalAsString) {
        this.FilePath = FilePath;
        this.startRow = startRow;
        this.endRow = endRow;
        this.colStatus = colStatus;
        this.colTanggal = colTanggal;
        this.colNoTbp = colNoTbp;
        this.tanggalAsString = tanggalAsString;
    }

    public void setFilePath(String FilePath) { this.FilePath = FilePath; }
    public void setStartRow(String startRow) { this.startRow = startRow; }
    public void setEndRow(String endRow) { this.endRow = endRow; }
    public void setColStatus(String colStatus) { this.colStatus =  colStatus; }
    public void setColTanggal(String colTanggal) { this.colTanggal = colTanggal; }
    public void setColNoTbp(String colNoTbp) { this.colNoTbp = colNoTbp; }
    public void setTanggalAsString(boolean tanggalAsString) { this.tanggalAsString = tanggalAsString; }
    public String getFilePath() { return this.FilePath; }
    public String getstartRow() { return this.startRow; }
    public String getendRow() { return this.endRow; }
    public String getColTanggal() { return this.colTanggal; }
    public String getColStatus() { return this.colStatus; }
    public String getColNoTbp() { return this.colNoTbp; }
    public boolean getTanggalAsString() { return this.tanggalAsString; }
    public void  setLinkUrl(String linkUrl) {this.linkUrl = linkUrl; }
    public String getLinkUrl() { return this.linkUrl; }

    public static DatabaseModel readConfig(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();

            return gson.fromJson(reader, DatabaseModel.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveConfig(DatabaseModel model, String configFile) {
        Gson gson = new Gson();
        String json = gson.toJson(model);

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
