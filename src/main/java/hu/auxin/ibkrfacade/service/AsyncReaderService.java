package hu.auxin.ibkrfacade.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AsyncReaderService {

    @Async
    public void processMessages(EClientSocket client, EReaderSignal readerSignal) {

        final EReader reader = new EReader(client, readerSignal);
        reader.start();

        while (client.isConnected()) {
            readerSignal.waitForSignal();
            try {
                reader.processMsgs();
                // log.info("msgs received");
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        log.info("client is not connected");
    }
}