package com.example.tets2;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class Doverennost {
    private LocalDate firstDate;
    private String city;
    private String ops;
    private String name;
    private String address;
    private String notWorkingDays;
    private String abonentBox;
    private String radioMain;
    private String radio2;
    private String contract4Num;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate contract4Date;
    private String contract5Num;
    private LocalDate contract5Date;
    private String radio6;
    private String workTime;
    private String phoneCode;
    private String number;

    private String bossPosition;
    private String bossFio;
    private String bossDocument;
    private boolean toAddWorkers;

    private String worker1Position;
    private String worker1Fio;
    private String worker2Position;
    private String worker2Fio;
    private String worker3Position;
    private String worker3Fio;
    private String worker4Position;
    private String worker4Fio;
    private String worker5Position;
    private String worker5Fio;

    private LocalDate previousDate;
    private LocalDate newDate;
    private boolean placeMax;
    private boolean needEmail;
    private String Email;

    public Doverennost() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getOps() {
        return ops;
    }

    public void setOps(String ops) {
        this.ops = ops;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAbonentBox() {
        return abonentBox;
    }

    public void setAbonentBox(String abonentBox) {
        this.abonentBox = abonentBox;
    }


    public String getContract4Num() {
        return contract4Num;
    }

    public void setContract4Num(String contract4Num) {
        this.contract4Num = contract4Num;
    }



    public String getContract5Num() {
        return contract5Num;
    }

    public void setContract5Num(String contract5Num) {
        this.contract5Num = contract5Num;
    }



    public String getWorkTime() {
        return workTime;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime;
    }


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBossPosition() {
        return bossPosition;
    }

    public void setBossPosition(String bossPosition) {
        this.bossPosition = bossPosition;
    }

    public String getBossFio() {
        return bossFio;
    }

    public String getBossDocument() {
        return bossDocument;
    }

    public void setBossDocument(String bossDocument) {
        this.bossDocument = bossDocument;
    }

    public boolean isToAddWorkers() {
        return toAddWorkers;
    }

    public void setToAddWorkers(boolean toAddWorkers) {
        this.toAddWorkers = toAddWorkers;
    }

    public String getWorker1Position() {
        return worker1Position;
    }

    public void setWorker1Position(String worker1Position) {
        this.worker1Position = worker1Position;
    }

    public String getWorker1Fio() {
        return worker1Fio;
    }

    public void setWorker1Fio(String worker1Fio) {
        this.worker1Fio = worker1Fio;
    }

    public String getWorker2Position() {
        return worker2Position;
    }

    public void setWorker2Position(String worker2Position) {
        this.worker2Position = worker2Position;
    }

    public String getWorker2Fio() {
        return worker2Fio;
    }

    public void setWorker2Fio(String worker2Fio) {
        this.worker2Fio = worker2Fio;
    }

    public String getWorker3Position() {
        return worker3Position;
    }

    public void setWorker3Position(String worker3Position) {
        this.worker3Position = worker3Position;
    }

    public String getWorker3Fio() {
        return worker3Fio;
    }

    public void setWorker3Fio(String worker3Fio) {
        this.worker3Fio = worker3Fio;
    }

    public String getWorker4Position() {
        return worker4Position;
    }

    public void setWorker4Position(String worker4Position) {
        this.worker4Position = worker4Position;
    }

    public String getWorker4Fio() {
        return worker4Fio;
    }

    public void setWorker4Fio(String worker4Fio) {
        this.worker4Fio = worker4Fio;
    }

    public String getWorker5Position() {
        return worker5Position;
    }

    public void setWorker5Position(String worker5Position) {
        this.worker5Position = worker5Position;
    }

    public String getWorker5Fio() {
        return worker5Fio;
    }

    public void setWorker5Fio(String worker5Fio) {
        this.worker5Fio = worker5Fio;
    }


    public boolean isPlaceMax() {
        return placeMax;
    }

    public void setPlaceMax(boolean placeMax) {
        this.placeMax = placeMax;
    }


    public LocalDate getFirstDate() {
        return firstDate;
    }

    public void setFirstDate(LocalDate firstDate) {
        this.firstDate = firstDate;
    }

    public LocalDate getContract5Date() {
        return contract5Date;
    }

    public void setContract5Date(LocalDate contract5Date) {
        this.contract5Date = contract5Date;
    }

    public LocalDate getContract4Date() {
        return contract4Date;
    }

    public void setContract4Date(LocalDate contract4Date) {
        this.contract4Date = contract4Date;
    }

    public LocalDate getPreviousDate() {
        return previousDate;
    }

    public void setPreviousDate(LocalDate previousDate) {
        this.previousDate = previousDate;
    }

    public LocalDate getNewDate() {
        return newDate;
    }

    public void setNewDate(LocalDate newDate) {
        this.newDate = newDate;
    }

    public String getNotWorkingDays() {
        return notWorkingDays;
    }

    public void setNotWorkingDays(String notWorkingDays) {
        this.notWorkingDays = notWorkingDays;
    }

    public void setBossFio(String bossFio) {
        this.bossFio = bossFio;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getRadioMain() {
        return radioMain;
    }

    public void setRadioMain(String radioMain) {
        this.radioMain = radioMain;
    }

    public String getRadio2() {
        return radio2;
    }

    public void setRadio2(String radio2) {
        this.radio2 = radio2;
    }

    public String getRadio6() {
        return radio6;
    }

    public void setRadio6(String radio6) {
        this.radio6 = radio6;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public boolean isNeedEmail() {
        return needEmail;
    }

    public void setNeedEmail(boolean needEmail) {
        this.needEmail = needEmail;
    }
}
