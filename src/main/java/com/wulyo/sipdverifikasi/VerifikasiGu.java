package com.wulyo.sipdverifikasi;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.PlaywrightException;

public class VerifikasiGu extends Thread {
    private Page page;
    private BrowserContext browserContext;
    private DatabaseModel databaseModel;
    private DriverSignal driverSignal;

    public VerifikasiGu(Page page, BrowserContext browserContext, DatabaseModel databaseModel, DriverSignal driverSignal) {
        this.page = page;
        this.browserContext = browserContext;
        this.databaseModel = databaseModel;
        this.driverSignal = driverSignal;
    }

    @Override
    public void run() {
        try {
            page.navigate(databaseModel.getLinkUrl());
            page.waitForLoadState(LoadState.LOAD);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            driverSignal.onsetStatus("Cek login!");



        } catch (Exception ex) {
            driverSignal.onsetStatus(ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }
}
