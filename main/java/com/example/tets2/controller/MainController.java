package com.example.tets2.controller;

import com.example.tets2.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class MainController {
    private Logger logger=Logger.getLogger(Tets2Application.class.getName());

    public static int counter = 0;//счётчик заявлений за день
    private boolean timeToCheckLogin=true;//требуется ли получать данные администраторов из базы данных
    @GetMapping("/download")//Get запрос для скачивания заявления
    public ResponseEntity download(@RequestParam("file") String file) throws Exception {
        Path path = Paths.get("src/main/resources/resultFiles/" + file + ".pdf");//путь е созданному файлу
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);//передать файл
    }

    @GetMapping("/login")
    public String loginGet() throws Exception {
        if (timeToCheckLogin)
        {
            timeToCheckLogin=false;
            ArrayList<AdminData> list=SQLConnector.GetAdmins();//получить данные администраторов
            if (list!=null){
                WebSecurityConfig.addUserDetails(list);//добавить данные в систему авторизации
            }
        }
        return "login";//перейти на страницу входа
    }
    @GetMapping("/adminPage")//Get запрос при входе на страницу администратора
    public String adminGet(Model model) throws Exception {
        model.addAttribute("errorList", new ErrorLog());
        model.addAttribute("logList", "");
        return "adminPage";
    }
    @PostMapping("/adminPageErrors")//Post запрос на получение списка ошибок администратором
    public String adminPost(Model model) throws Exception {
        ArrayList<ErrorLog> list;
        UserDetails details=(UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        list=SQLConnector.GetErrors(details.getUsername());
        if (list!=null)//если список ошибок не null
        {
            model.addAttribute("errorList",list.toArray());//вывести список
            model.addAttribute("empty",false);
        }
        else {
            model.addAttribute("empty",true);//вывести текст, что нет ошибок
        }
        return "adminPage";
    }
    @PostMapping("/adminPageLogs")//Страница списка лог файлов
    public String adminLogsPost(Model model) throws Exception {
        ArrayList<String> list =new ArrayList<>();
        File folder = new File("src/main/resources/logs");
        File[] listOfFiles = folder.listFiles();//список файлов
        if(listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()&&!listOfFiles[i].getName().contains("lck")) {
                    list.add(listOfFiles[i].getName());//добавить название файла в список
                }
            }
        }
        if (list.size()!=0)
        {
            model.addAttribute("logList",list.toArray());//вернуть список
            model.addAttribute("emptyLogs",false);
        }
        else {
            model.addAttribute("emptyLogs",true);//вывести текст, что нет лог файлов
        }
        return "adminPage";
    }
    @GetMapping("/adminPageLogs/download")//скачивание лог файла
    public ResponseEntity downloadLogs(@RequestParam("file") String file) throws Exception {
        Path path = Paths.get("src/main/resources/logs/" + file);//выбранных файл
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() +"\"")
                .body(resource);//передача файла
    }
    @GetMapping("/doverennost")//страница заявления-доверенности
    public String doverennostGet(Model model) {
        logger.logp(Level.INFO, "MainController", "doverennostGet", "Переход на станицу заявления-доверенности");
        model.addAttribute("error", null);
        model.addAttribute("data", new Doverennost());
        return "doverennost";
    }

    @PostMapping("/doverennost")//обработка формы заявления-доверенности
    public String doverennostPost(@ModelAttribute("data") Doverennost data, Model model) throws Exception {
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Получен запрос POST на заполение заявления-доверенности");
        Map<String, String> toPrint = new HashMap<>();//словарь для хранения значений полей
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода даты подачи");
        if (data.getFirstDate() == null) {//поле дата подачи
            model.addAttribute("error", "В поле дата подачи заявления не введена дата");
            return "doverennost";
        }
        toPrint.put("currentDateDay", makeDate(data.getFirstDate().getDayOfMonth()));
        toPrint.put("currentDateMonth", makeDate(data.getFirstDate().getMonthValue()));
        String year = Integer.toString(data.getFirstDate().getYear());
        toPrint.put("currentDateYear", year.substring(2));
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода города");
        if (checkStringEmpty(data.getCity()))//поле населённый пункт
            toPrint.put("city", data.getCity().trim());
        else {
            model.addAttribute("error", "В поле населённый пункт не введено значение");
            return "doverennost";
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода номера ОПС");
        if (checkStringEmpty(data.getOps()))//поле номер ОПС
            toPrint.put("officeNumber", data.getOps().trim());
        else {
            model.addAttribute("error", "В поле обслуживающее ОПС не введено значение");
            return "doverennost";
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода названия");
        if (checkStringEmpty(data.getName()))//поле название
            toPrint.put("name", data.getName().trim());
        else {
            model.addAttribute("error", "В поле название не введено значение");
            return "doverennost";
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода адреса");
        if (checkStringEmpty(data.getAddress()))//поле адрес
            toPrint.put("address", data.getAddress().trim());
        else {
            model.addAttribute("error", "В поле почтовый адрес не введено значение");
            return "doverennost";
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода выходных");
        if (checkStringEmpty(data.getNotWorkingDays()))//поле выходные
            toPrint.put("weekends", data.getNotWorkingDays().trim());
        else {
            model.addAttribute("error", "В поле выходные не введено значение");
            return "doverennost";
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка выбора способа доставки 1-5");
        if (!checkSelectedRadioAll(data)) { //проверка выбора пунктов 1-5
            model.addAttribute("error", "В пунктах 1-5 не выбран способ доставки");
            return "doverennost";
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка выбора способа доставки 6");
        if (!checkPar6Radio(data)) {//проверка выбора пункта 6
            model.addAttribute("error", "Способ доставки Netлистов в пункте 6 должен быть выбран");
            return "doverennost";

        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка выбора способа доставки 1 и 2");
        if (data.getRadio2() != null) { //проверка выбора пунктов 1 и 2
            if (!checkSelectedRadioFirst(data)) {
                model.addAttribute("error", "При выбранном способе доставке из пункта 1, должен быть выбран и пункт 2");
                return "doverennost";
            }
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Установка выбора способа доставки 1-5");
        switch (data.getRadioMain()) { //проверка выбора пунктов 1-5
            case "radio11": //проверка выбора пункта 1-1
                if (!checkSelectedRadioFirst(data)) {
                    model.addAttribute("error", "При выбранном способе доставке из пункта 1, должен быть выбран и пункт 2");
                    return "doverennost";
                }
                if (data.getRadio2().equals("radio21")) {
                    toPrint.put("ticP2N1", "X");
                } else {
                    toPrint.put("ticP2N2", "X");
                }
                toPrint.put("ticP1N1", "X");
                if (checkInt(data.getAbonentBox()))
                    toPrint.put("boxNumber", data.getAbonentBox());
                else {
                    model.addAttribute("error", "В поле номер абонентского ящика введено не верное значение");
                    return "doverennost";
                }
                break;
            case "radio12"://проверка выбора пункта 1-2
                if (data.getRadio2().equals("radio21")) {
                    toPrint.put("ticP2N1", "X");
                } else {
                    toPrint.put("ticP2N2", "X");
                }
                toPrint.put("ticP1N2", "X");
                break;
            case "radio13"://проверка выбора пункта 1-3
                if (data.getRadio2().equals("radio21")) {
                    toPrint.put("ticP2N1", "X");
                } else {
                    toPrint.put("ticP2N2", "X");
                }
                toPrint.put("ticP1N3", "X");

                break;
            case "radio3"://проверка выбора пункта 3
                toPrint.put("ticP3", "X");
                break;
            case "radio4"://проверка выбора пункта 4
                toPrint.put("ticP4", "X");
                if (checkInt(data.getContract4Num()))
                    toPrint.put("contractNumberP4", data.getContract4Num());
                else {
                    model.addAttribute("error", "В поле номер договора доставки курьером введено не верное значение");
                    return "doverennost";
                }
                if (data.getContract4Date() == null) {
                    model.addAttribute("error", "В поле дата договора доставки курьером не введена дата");
                    return "doverennost";
                }

                if (!checkDate(data.getContract4Date())) {
                    model.addAttribute("error", "В поле дата договора доставки курьером введена будущая дата");
                    return "doverennost";
                }

                LocalDate contractDate = data.getContract4Date();
                toPrint.put("contractDateDayP4", makeDate(contractDate.getDayOfMonth()));
                toPrint.put("contractDateMonthP4", makeDate(contractDate.getMonthValue()));
                toPrint.put("contractDateYearP4", (Integer.toString(contractDate.getYear())).substring(2));
                break;
            case "radio5"://проверка выбора пункта 5
                toPrint.put("ticP5", "X");
                if (checkInt(data.getContract5Num()))
                    toPrint.put("contractNumberP5", data.getContract5Num());
                else {
                    model.addAttribute("error", "В поле номер договора доставки на этаж введено не верное значение");
                    return "doverennost";
                }
                if (data.getContract4Date() == null) {
                    model.addAttribute("error", "В поле дата договора доставки на этаж не введена дата");
                    return "doverennost";
                }

                if (!checkDate(data.getContract5Date())) {
                    model.addAttribute("error", "В поле дата договора доставки на этаж введена будущая дата");
                    return "doverennost";
                }
                contractDate = data.getContract5Date();
                toPrint.put("contractDateDayP5", makeDate(contractDate.getDayOfMonth()));
                toPrint.put("contractDateMonthP5", makeDate(contractDate.getMonthValue()));
                toPrint.put("contractDateYearP5", (Integer.toString(contractDate.getYear())).substring(2));
                break;
            default:
                break;
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "установка выбора способа доставки 6");

        if (data.getRadio6().equals("radio61")) {//проверка выбора пункта 6-1

            toPrint.put("ticP6N1", "X");
            if (checkStringEmpty(data.getWorkTime()))
                toPrint.put("workingTimeP6", data.getWorkTime().trim());
            else {
                model.addAttribute("error", "В поле время работы введено не верное значение");
                return "doverennost";
            }
        } else {//проверка выбора пункта 6-2

            toPrint.put("ticP6N2", "X");
            if (checkInt(data.getPhoneCode()))
                toPrint.put("phoneNumberCodeP6", data.getPhoneCode().trim());
            else {
                model.addAttribute("error", "В поле код городского телефона введено не верное значение");
                return "doverennost";
            }
            if (checkInt(data.getNumber()))
                toPrint.put("phoneNumberBaseP6", data.getNumber().trim());
            else {
                model.addAttribute("error", "В поле номер городского телефона введено не верное значение");
                return "doverennost";
            }
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода руководителя");
        String postToCheck;
        if (checkStringEmpty(data.getBossPosition()) && checkStringEmpty(data.getBossFio())) {//проверка поля руководителя
            toPrint.put("receiverFIO", data.getBossPosition().trim() + " " + data.getBossFio().trim());
            postToCheck = data.getBossPosition();
        } else {
            model.addAttribute("error", "В поля должность получателя и/или фамилия, инициалы получателя не введено значение");
            return "doverennost";
        }
        if (checkStringEmpty(data.getBossDocument()))//проверка поля докумен руководителя
            toPrint.put("documentName", data.getBossDocument().trim());
        else {
            model.addAttribute("error", "В поле документ, на основании которого действует получатель не введено значение");
            return "doverennost";
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода уполномоченных");
        if (data.isToAddWorkers()) { //проверка выбран ли пункт уполномачивания сотрудников
            if (!data.getWorker1Fio().isEmpty() || !data.getWorker1Position().isEmpty()) {//проверка полей первого сотрудника
                if (checkStringEmpty(data.getWorker1Fio()) && checkStringEmpty(data.getWorker1Position())) {
                    if (checkMainAndEmployeePost(postToCheck, data.getWorker1Position())) {
                        model.addAttribute("error", "Получатель не может уполномочить себя на получение писем, только своих сотрудников (поле 1)");
                        return "doverennost";
                    }
                    toPrint.put("postN1", data.getWorker1Position().trim());
                    toPrint.put("acceptedN1", data.getWorker1Fio().trim());
                } else {
                    model.addAttribute("error", "Поля должность сотрудника 1 и ФИО сотрудника 1 должны быть заполнены вместе");
                    return "doverennost";
                }
            }
            if (!data.getWorker2Fio().isEmpty() || !data.getWorker2Position().isEmpty()) {//проверка полей второго сотрудника
                if (checkStringEmpty(data.getWorker2Fio()) && checkStringEmpty(data.getWorker2Position())) {
                    if (checkMainAndEmployeePost(postToCheck, data.getWorker2Position())) {
                        model.addAttribute("error", "Получатель не может уполномочить себя на получение писем, только своих сотрудников (поле 2)");
                        return "doverennost";
                    }
                    toPrint.put("postN2", data.getWorker2Position().trim());
                    toPrint.put("acceptedN2", data.getWorker2Fio().trim());
                } else {
                    model.addAttribute("error", "Поля должность сотрудника 2 и ФИО сотрудника 2 должны быть заполнены вместе");
                    return "doverennost";
                }
            }
            if (!data.getWorker3Fio().isEmpty() || !data.getWorker3Position().isEmpty()) {//проверка полей трельего сотрудника
                if (checkStringEmpty(data.getWorker3Fio()) && checkStringEmpty(data.getWorker3Position())) {
                    if (checkMainAndEmployeePost(postToCheck, data.getWorker3Position())) {
                        model.addAttribute("error", "Получатель не может уполномочить себя на получение писем, только своих сотрудников (поле 3)");
                        return "doverennost";
                    }
                    toPrint.put("postN3", data.getWorker3Position().trim());
                    toPrint.put("acceptedN3", data.getWorker3Fio().trim());
                } else {
                    model.addAttribute("error", "Поля должность сотрудника 3 и ФИО сотрудника 3 должны быть заполнены вместе");
                    return "doverennost";
                }
            }
            if (!data.getWorker4Fio().isEmpty() || !data.getWorker4Position().isEmpty()) {//проверка полей четвёртого сотрудника
                if (checkStringEmpty(data.getWorker4Fio()) && checkStringEmpty(data.getWorker4Position())) {
                    if (checkMainAndEmployeePost(postToCheck, data.getWorker4Position())) {
                        model.addAttribute("error", "Получатель не может уполномочить себя на получение писем, только своих сотрудников (поле 4)");
                        return "doverennost";
                    }
                    toPrint.put("postN4", data.getWorker4Position().trim());
                    toPrint.put("acceptedN4", data.getWorker4Fio().trim());
                } else
                    model.addAttribute("error", "Поля должность сотрудника 4 и ФИО сотрудника 4 должны быть заполнены вместе");
                return "doverennost";
            }
            if (!data.getWorker5Fio().isEmpty() || !data.getWorker5Position().isEmpty()) {//проверка полей пятого сотрудника
                if (checkStringEmpty(data.getWorker5Fio()) && checkStringEmpty(data.getWorker5Position())) {
                    if (checkMainAndEmployeePost(postToCheck, data.getWorker5Position())) {
                        model.addAttribute("error", "Получатель не может уполномочить себя на получение писем, только своих сотрудников (поле 5)");
                        return "doverennost";
                    }
                    toPrint.put("postN5", data.getWorker5Position().trim());
                    toPrint.put("acceptedN5", data.getWorker5Fio().trim());
                } else {
                    model.addAttribute("error", "Поля должность сотрудника 5 и ФИО сотрудника 5 должны быть заполнены вместе");
                    return "doverennost";
                }
            }
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода даты прошлого заявления");
        if (data.getPreviousDate() != null) {//проверка поля дата прошлого заявления
            if (!checkDate(data.getPreviousDate())) {
                model.addAttribute("error", "В поле дата прошлого заявления-доверенности введена будущая дата");
                return "doverennost";
            }
            LocalDate previousDate = data.getPreviousDate();
            toPrint.put("previousDateDay", makeDate(previousDate.getDayOfMonth()));
            toPrint.put("previousDateMonth", makeDate(previousDate.getMonthValue()));
            toPrint.put("previousDateYear", (Integer.toString(previousDate.getYear())).substring(2));
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода срока действия");
        LocalDate expiryDate;
        if (data.isPlaceMax()) {//проверка поля пункта установки мактимального срока действия
            expiryDate = data.getFirstDate().plusYears(3).minusDays(1);
        } else {//проверка поля срока действия
            LocalDate toCheck = data.getNewDate();

            if (toCheck == null) {
                model.addAttribute("error", "В поле срок действия не введено значение");
                return "doverennost";
            }
            LocalDate now = LocalDate.now();
            if (now.isAfter(toCheck) || now.isEqual(toCheck)) {
                model.addAttribute("error", "В поле срок действия введена прошлая дата");
                return "doverennost";
            }
            toCheck = toCheck.plusYears(3).minusDays(1);
            if (now.isAfter(toCheck)) {
                model.addAttribute("error", "В поле срок действия введено значение более 3 лет");
                return "doverennost";
            }
            expiryDate = data.getNewDate();
        }
        toPrint.put("expireDateDay", makeDate(expiryDate.getDayOfMonth()));
        toPrint.put("expireDateMonth", makeDate(expiryDate.getMonthValue()));
        toPrint.put("expireDateYear", (Integer.toString(expiryDate.getYear())).substring(2));
        toPrint.put("receiverPostEnd", data.getBossPosition().trim());
        toPrint.put("receiverFioEnd", data.getBossFio().trim());
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка ввода email");
        if (data.isNeedEmail()) {//проверка выбора пункта отправки уведомления
            if (!data.getEmail().isEmpty() && EmailHandler.ValidateEmail(data.getEmail())) {
                try {//добавление уведомления в базу данных
                    SQLConnector.AddToDB(data.getName(), data.getEmail(), expiryDate,1);
                } catch (SQLException ex) {
                    model.addAttribute("error", "Произошла ошибка при сохранении в базу данных");
                    ErrorLog log=new ErrorLog(SQLConnector.getErrorId(),ex.getErrorCode(),ex.getMessage());
                    Tets2Application.databaseErrors.add(log);
                    SQLConnector.errorCount++;
                    return "doverennost";
                }
            } else {
                model.addAttribute("error", "Введите корректный адрес электронной почты");
                return "doverennost";
            }
        }
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Старт создания файла");
        counter++;
        String downloadName = PDFFiller.printDocument(toPrint, counter, 1);//создание заявления
        logger.logp(Level.INFO, "MainController", "doverennostPost", "Проверка создания файла");

        if (downloadName == null) {//проверка создания заявления
            model.addAttribute("error", "Произошла ошибка на сервере, повторите попытку позже");
            logger.logp(Level.SEVERE, "MainController", "doverennostPost", "Файл отсутствует");
            return "doverennost";
        } else
        {
            logger.logp(Level.INFO, "MainController", "doverennostPost", "Перенаправление на скачивание файла");
            return String.format("redirect:/download?file=%s", downloadName.substring(0, downloadName.length() - 4));
        }
    }

    @GetMapping("/dosilFiz")//страница заявления о досыле для физических лиц
    public String dosilFizGet(Model model) {
        logger.logp(Level.INFO, "MainController", "dosilFizGet", "Переход на станицу заявления о досыле (физ)");
        model.addAttribute("error", null);
        model.addAttribute("data", new Dosil());
        return "dosilFiz";
    }

    @PostMapping("/dosilFiz")//обработка формы страницы заявления о досыле для физических лиц
    public String dosilFizPost(@ModelAttribute("data") Dosil data, Model model) throws Exception {
        Map<String, String> toPrint = new HashMap<>();
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Получен запрос POST на заполение заявления-доверенности");

        if (data.getFirstDate() == null) {//поле дата подачи
            model.addAttribute("error", "В поле дата подачи заявления не введена дата");
            return "dosilFiz";
        }
        toPrint.put("currentDate", String.format("%s.%s.%d", makeDate(data.getFirstDate().getDayOfMonth()), makeDate(data.getFirstDate().getMonthValue()), data.getFirstDate().getYear()));
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Проверка ввода номера ОПС");
        if (checkStringEmpty(data.getOps()))//поле номер ОПС
            toPrint.put("officeNumber", data.getOps().trim());
        else {
            model.addAttribute("error", "В поле обслуживающее ОПС не введено значение");
            return "dosilFiz";
        }
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Проверка ввода названия");
        if (checkStringEmpty(data.getName()))//поле названия
            toPrint.put("fio", data.getName().trim());
        else {
            model.addAttribute("error", "В поле ФИО не введено значение");
            return "dosilFiz";
        }
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Проверка ввода старого адреса");
        if (checkStringEmpty(data.getAddressOld()))//поле старого адреса
            toPrint.put("oldAddress", data.getAddressOld().trim());
        else {
            model.addAttribute("error", "В поле адрес, на который поступают отправления, не введено значение");
            return "dosilFiz";
        }
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Проверка ввода нового адреса");
        if (checkStringEmpty(data.getAddressNew()))//поле нового адреса
            toPrint.put("newAddress", data.getAddressNew().trim());
        else {
            model.addAttribute("error", "В поле адрес, на который требуется досылать отправления, не введено значение");
            return "dosilFiz";
        }
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Проверка выбора отправлений для досыла");
        if (!checkSelectedCheck(data)) {//проверка выбора отправлений
            model.addAttribute("error", "Не выбраны почтовые отправления для досыла");
            return "dosilFiz";
        }
        if (data.isCheckAll()) {//заполнение выбора отправлений
            toPrint.put("checkAll", "X");
        } else {
            if (data.isCheck1()) {
                toPrint.put("check1", "X");
            }
            if (data.isCheck2()) {
                toPrint.put("check2", "X");
            }
            if (data.isCheck3()) {
                toPrint.put("check3", "X");
            }
            if (data.isCheck4()) {
                toPrint.put("check4", "X");
            }
            if (data.isCheck5()) {
                toPrint.put("check5", "X");
            }
            if (data.isCheck6()) {
                toPrint.put("check6", "X");
            }
            if (data.isCheck7()) {
                toPrint.put("check7", "X");
            }
            if (data.isCheck8()) {
                toPrint.put("check8", "X");
            }
        }

        LocalDate expiryDate;
        LocalDate toCheck = data.getStartDate();
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Проверка ввода начала срока действия");

        if (toCheck == null) {//поле дата начала срока досыла
            model.addAttribute("error", "В поле начало срока действия не введено значение");
            return "dosilFiz";
        }
        if (data.getFirstDate().isAfter(toCheck)) {
            model.addAttribute("error", "Дата подачи заявления не может быть позже даты начала срока действия");
            return "dosilFiz";
        }
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Проверка ввода конца срока действия");

        if (data.isPlaceMax()) {//поле дата конца срока досыла
            expiryDate = data.getStartDate().plusMonths(6).minusDays(1);
        } else {

            LocalDate now = LocalDate.now();
            if (now.isAfter(toCheck) || now.isEqual(toCheck)) {
                model.addAttribute("error", "В поле срок действия введена прошлая дата");
                return "dosilFiz";
            }

            toCheck = toCheck.plusMonths(6).minusDays(1);
            if (now.isAfter(toCheck)) {
                model.addAttribute("error", "В поле срок действия введено значение более 3 лет");
                return "dosilFiz";
            }
            expiryDate = data.getEndDate();
        }
        toPrint.put("periodToDay", makeDate(data.getStartDate().getDayOfMonth()));
        toPrint.put("periodToMonth", makeDate(data.getStartDate().getMonthValue()));
        toPrint.put("periodToYear", (Integer.toString(data.getStartDate().getYear())).substring(2));
        toPrint.put("periodFromDay", makeDate(expiryDate.getDayOfMonth()));
        toPrint.put("periodFromMonth", makeDate(expiryDate.getMonthValue()));
        toPrint.put("periodFromYear", (Integer.toString(expiryDate.getYear())).substring(2));
        toPrint.put("fioShort", makeFioShort(data.getName()));
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Проверка ввода email");
        if (data.isNeedEmail()) {//проверка надобности сохранения email
            if (!data.getEmail().isEmpty() && EmailHandler.ValidateEmail(data.getEmail())) {
                try {
                    SQLConnector.AddToDB(data.getName(), data.getEmail(), expiryDate,2);
                } catch (SQLException ex) {
                    model.addAttribute("error", "Произошла ошибка при сохранении в базу данных");
                    ErrorLog log=new ErrorLog(SQLConnector.getErrorId(),ex.getErrorCode(),ex.getMessage());
                    Tets2Application.databaseErrors.add(log);
                    SQLConnector.errorCount++;
                    return "dosilFiz";
                }
            } else {
                model.addAttribute("error", "Введите корректный адрес электронной почты");
                return "dosilFiz";
            }
        }
        counter++;
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Старт создания файла");
        String downloadName = PDFFiller.printDocument(toPrint, counter, 2);//создание заявления
        logger.logp(Level.INFO, "MainController", "dosilFizPost", "Проверка создания файла");
        if (downloadName == null) {
            model.addAttribute("error", "Произошла ошибка на сервере, повторите попытку позже");
            logger.logp(Level.SEVERE, "MainController", "dosilFizPost", "Файл отсутствует");
            return "dosilFiz";
        } else {
            logger.logp(Level.INFO, "MainController", "dosilFizPost", "Перенаправление на скачивание файла");
            return String.format("redirect:/download?file=%s", downloadName.substring(0, downloadName.length() - 4));
        }
    }

    @GetMapping("/dosilYur")//страница досыла юридических лиц
    public String dosilYurGet(Model model) {
        logger.logp(Level.INFO, "MainController", "dosilYurGet", "Переход на станицу заявления о досыле (юр)");
        model.addAttribute("error", null);
        model.addAttribute("data", new DosilYur());
        return "dosilYur";
    }



    @PostMapping("/dosilYur")//обработка формы страницы досыла юридических лиц
    public String dosilYurPost(@ModelAttribute("data") DosilYur data, Model model) throws Exception {
        Map<String, String> toPrint = new HashMap<>();
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Получен запрос POST на заполение заявления-доверенности");

        if (data.getFirstDate() == null) {//дата подачи
            model.addAttribute("error", "В поле дата подачи заявления не введена дата");
            return "dosilYur";
        }
        toPrint.put("currentDate", String.format("%s.%s.%d", makeDate(data.getFirstDate().getDayOfMonth()), makeDate(data.getFirstDate().getMonthValue()), data.getFirstDate().getYear()));
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Проверка ввода номера ОПС");
        if (checkStringEmpty(data.getOps()))//номер ОПС
            toPrint.put("officeNumber", data.getOps().trim());
        else {
            model.addAttribute("error", "В поле обслуживающее ОПС не введено значение");
            return "dosilYur";
        }
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Проверка ввода названия");
        if (checkStringEmpty(data.getName()))//Название
            toPrint.put("fio", data.getName().trim());
        else {
            model.addAttribute("error", "В поле название не введено значение");
            return "dosilYur";
        }
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Проверка ввода старого адреса");
        if (checkStringEmpty(data.getAddressOld()))//старый адрес
            toPrint.put("oldAddress", data.getAddressOld().trim());
        else {
            model.addAttribute("error", "В поле адрес, на который поступают отправления, не введено значение");
            return "dosilYur";
        }
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Проверка ввода нового адреса");
        if (checkStringEmpty(data.getAddressNew()))//Новый адрес
            toPrint.put("newAddress", data.getAddressNew().trim());
        else {
            model.addAttribute("error", "В поле адрес, на который требуется досылать отправления, не введено значение");
            return "dosilYur";
        }
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Проверка выбора отправлений для досыла");
        if (!checkSelectedCheck2(data)) {//Проверка выбора отправлений
            model.addAttribute("error", "Не выбраны почтовые отправления для досыла");
            return "dosilYur";
        }
        if (data.isCheckAll()) {//заполнение выбора отправлений
            toPrint.put("checkAll", "X");
        } else {
            if (data.isCheck1()) {
                toPrint.put("check1", "X");
            }
            if (data.isCheck2()) {
                toPrint.put("check2", "X");
            }
            if (data.isCheck3()) {
                toPrint.put("check3", "X");
            }
            if (data.isCheck4()) {
                toPrint.put("check4", "X");
            }
            if (data.isCheck5()) {
                toPrint.put("check5", "X");
            }
            if (data.isCheck6()) {
                toPrint.put("check6", "X");
            }
            if (data.isCheck7()) {
                toPrint.put("check7", "X");
            }
            if (data.isCheck8()) {
                toPrint.put("check8", "X");
            }
        }

        LocalDate expiryDate;
        LocalDate toCheck = data.getStartDate();
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Проверка ввода начала срока действия");

        if (toCheck == null) {//начало срока действия
            model.addAttribute("error", "В поле начало срока действия не введено значение");
            return "dosilYur";
        }
        if (data.getFirstDate().isAfter(toCheck)) {
            model.addAttribute("error", "Дата подачи заявления не может быть позже даты начала срока действия");
            return "dosilYur";
        }
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Проверка ввода конца срока действия");
        if (data.isPlaceMax()) {//конец срока действия
            expiryDate = data.getStartDate().plusMonths(6).minusDays(1);
        } else {

            LocalDate now = LocalDate.now();
            if (now.isAfter(toCheck) || now.isEqual(toCheck)) {
                model.addAttribute("error", "В поле срок действия введена прошлая дата");
                return "dosilYur";
            }

            toCheck = toCheck.plusMonths(6).minusDays(1);
            if (now.isAfter(toCheck)) {
                model.addAttribute("error", "В поле срок действия введено значение более 3 лет");
                return "dosilYur";
            }
            expiryDate = data.getEndDate();
        }

        toPrint.put("periodToDay", makeDate(data.getStartDate().getDayOfMonth()));
        toPrint.put("periodToMonth", makeDate(data.getStartDate().getMonthValue()));
        toPrint.put("periodToYear", (Integer.toString(data.getStartDate().getYear())).substring(2));
        toPrint.put("periodFromDay", makeDate(expiryDate.getDayOfMonth()));
        toPrint.put("periodFromMonth", makeDate(expiryDate.getMonthValue()));
        toPrint.put("periodFromYear", (Integer.toString(expiryDate.getYear())).substring(2));
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Проверка ввода руководителя");
        if (!checkStringEmpty(data.getPostBoss())) {//должность руководителя
            model.addAttribute("error", "В поле должность руководителя введено значение");
            return "dosilYur";
        }
        if (!checkStringEmpty(data.getFioShort())) {//ФИО руководителя
            model.addAttribute("error", "В поле ФИО руководителя не введено значение");
            return "dosilYur";
        } else
            toPrint.put("fioShort", makeFioShort(data.getPostBoss() + " " + data.getFioShort()));
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Проверка ввода email");
        if (data.isNeedEmail()) {//проверка надобности email
            if (!data.getEmail().isEmpty() && EmailHandler.ValidateEmail(data.getEmail())) {
                try {
                    SQLConnector.AddToDB(data.getName(), data.getEmail(), expiryDate, 3);
                } catch (SQLException ex) {
                    model.addAttribute("error", "Произошла ошибка при сохранении в базу данных");
                    ErrorLog log = new ErrorLog(SQLConnector.getErrorId(), ex.getErrorCode(), ex.getMessage());
                    Tets2Application.databaseErrors.add(log);
                    SQLConnector.errorCount++;
                    return "dosilYur";
                }
            } else {
                model.addAttribute("error", "Введите корректный адрес электронной почты");
                return "dosilYur";
            }
        }
        counter++;
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Старт создания файла");
        String downloadName = PDFFiller.printDocument(toPrint, counter, 3);//создание заявления
        logger.logp(Level.INFO, "MainController", "dosilYurPost", "Проверка создания файла");
        if (downloadName == null) {
            model.addAttribute("error", "Произошла ошибка на сервере, повторите попытку позже");
            return "dosilYur";
        } else {
            logger.logp(Level.INFO, "MainController", "dosilYurPost", "Перенаправление на скачивание файла");
            return String.format("redirect:/download?file=%s", downloadName.substring(0, downloadName.length() - 4));
        }
    }

    private String makeDate(int date) {//привести дату в SQL формат
        String result;
        if (date < 10)
            result = String.format("0%d", date);
        else
            result = String.format("%d", date);
        return result;
    }
    private String makeFioShort(String fio) {//создать Фамилию и инициалы из ФИО полностью
        String[] fullName = fio.split(" ");
        StringBuilder result = new StringBuilder();
        result.append(fullName[0]).append(" ");
        for (int i = 1; i < fullName.length; i++) {
            result.append(fullName[i].trim(), 0, 1).append(" ");
        }
        return result.toString();
    }

    private boolean checkStringEmpty(String value) {
        return !value.trim().isEmpty();
    }//проверить пустую строку

    private boolean checkSelectedRadioAll(Doverennost data) {//проверить выбор способа доставки
        return data.getRadioMain() != null || data.getRadio2() != null;
    }

    private boolean checkSelectedRadioFirst(Doverennost data) {//Проверить способ доставки 1 и 2
        return (data.getRadioMain() != null && (data.getRadioMain().equals("radio11") || data.getRadioMain().equals("radio12") || data.getRadioMain().equals("radio13")) && data.getRadio2() != null);
    }

    private boolean checkSelectedCheck(Dosil data) {//проверить выбор отправлений для досыла
        return data.isCheckAll() || data.isCheck2() || data.isCheck3() || data.isCheck4() || data.isCheck5() || data.isCheck6() || data.isCheck7() || data.isCheck8() || data.isCheck1();

    }

    private boolean checkSelectedCheck2(DosilYur data) {//проверить выбор отправлений для досыла
        return data.isCheckAll() || data.isCheck2() || data.isCheck3() || data.isCheck4() || data.isCheck5() || data.isCheck6() || data.isCheck7() || data.isCheck8() || data.isCheck1();

    }
    private boolean checkPar6Radio(Doverennost data) {
        return data.getRadio6() != null;
    }//Проверить способ доставки 6

    private boolean checkInt(String value) {//проверить int
        try {
            Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    private boolean checkDate(LocalDate toCheck) {//проверить дату, если она позже
        LocalDate now = LocalDate.now();
        return !now.isBefore(toCheck);
    }

    private boolean checkMainAndEmployeePost(String mainPost, String employeePost) {//проверить записал
        return mainPost.equals(employeePost);// ли директор себя в уполномоченные лица
    }

}


