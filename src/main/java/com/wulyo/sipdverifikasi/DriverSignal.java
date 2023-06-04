package com.wulyo.sipdverifikasi;

public interface DriverSignal {
    void onBrowserOpen();
    void onDone();
    void onsetStatus(String status);

    void onAddTable(Object[] rowData);

    void onUpdateTable(String status);

    void onError(String error);

}
