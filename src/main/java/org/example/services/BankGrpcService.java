package org.example.services;

import io.grpc.stub.StreamObserver;
import org.example.stubs.Bank;
import org.example.stubs.BankServiceGrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.random;

public class BankGrpcService extends BankServiceGrpc.BankServiceImplBase {
    private static final Map<String, Double> currencies = new HashMap<>();

    static {
        currencies.put("USD", 1.0);
        currencies.put("EUR", 0.84); currencies.put("MAD", 1.4);
    }

    @Override
    public void convert (Bank.ConvertCurrencyRequest request, StreamObserver<Bank.ConvertCurrencyResponse> responseObserver) {
        String currencyFrom=request.getCurrencyFrom();
        String currencyTo=request.getCurrencyTo(); double amount=request.getAmount();
        double fromRate = currencies.get(currencyFrom);
        double toRate = currencies.get(currencyTo);
        double result = amount * fromRate / toRate;
        Bank.ConvertCurrencyResponse response = Bank.ConvertCurrencyResponse.newBuilder()
                .setCurrencyFrom(currencyFrom)
                .setCurrencyTo(currencyTo)
                .setAmount (amount)
                .setResult(result)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getStream(Bank.ConvertCurrencyRequest request, StreamObserver<Bank.ConvertCurrencyResponse> responseObserver) {
        String currencyFrom=request.getCurrencyFrom();
        String currencyTo=request.getCurrencyTo();
        double amount=request.getAmount();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int counter = 0;

            @Override
            public void run() {
                Bank.ConvertCurrencyResponse response = Bank.ConvertCurrencyResponse.newBuilder()
                        .setCurrencyFrom(currencyFrom)
                        .setCurrencyTo(currencyTo)
                        .setAmount(amount)
                        .setResult(amount * random() * 100)
                        .build();
                responseObserver.onNext(response);
                ++counter;
                if (counter == 20) {
                    responseObserver.onCompleted();
                    timer.cancel();
                }
            }
        },1000, 1000);
    }

    @Override
    public StreamObserver<Bank.ConvertCurrencyRequest> performStream(StreamObserver<Bank.ConvertCurrencyResponse> responseObserver) {
        return new StreamObserver<Bank.ConvertCurrencyRequest>() {
            double sum=0, result;

            @Override
            public void onNext(Bank.ConvertCurrencyRequest convertCurrencyRequest) {
                String currencyFrom = convertCurrencyRequest.getCurrencyFrom();
                String currencyTo = convertCurrencyRequest.getCurrencyTo();
                double amount = convertCurrencyRequest.getAmount();
                double fromRate = currencies.get(currencyFrom);
                double toRate = currencies.get(currencyTo);
                sum += convertCurrencyRequest.getAmount();
                result = sum * fromRate / toRate;
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                Bank.ConvertCurrencyResponse response = Bank.ConvertCurrencyResponse.newBuilder()
                        .setResult(result)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Bank.ConvertCurrencyRequest> fullCurrencyStream(StreamObserver<Bank.ConvertCurrencyResponse> responseObserver) {
        return new StreamObserver<Bank.ConvertCurrencyRequest>() {
            @Override
            public void onNext(Bank.ConvertCurrencyRequest convertCurrencyRequest) {
                Bank.ConvertCurrencyResponse response = Bank.ConvertCurrencyResponse.newBuilder()
                        .setResult(convertCurrencyRequest.getAmount() * random() * 80)
                        .build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
