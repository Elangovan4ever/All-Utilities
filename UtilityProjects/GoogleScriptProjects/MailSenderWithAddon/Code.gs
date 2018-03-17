//This script is written by Elangovan Manickam. For any queries or modifications, please contact: elangovan4ever@gmail.com

var SUBJECT_KEY='MailSubject';
var MESSAGE_KEY='MailBody';
var RECIPIENTS_TO_KEY='RecipientsTO';
var RECIPIENTS_CC_KEY='RecipientsCC';
var MAIL_FREQ_KEY='MailTiggerFreq';

var REMINDER_SUBJECT_KEY='ReminderSubject';
var REMINDER_MESSAGE_KEY='ReminderBody';
var REMINDER_RECIPIENTS_KEY='RecipientsReminder';

var sheetsNotToClear = ["SprintReport", "Chart", "CommonOptions", "Holidays", "History"];

var sheetsToUpdateEffortInChart = ["Kavya", "Elangovan","Saurabh", "Sukanya", "Sagar", "Simpi", "Vikash", "Ram", "Abira", "Paras"];
var effortUpdatedColumnNumber = [7, 7, 7, 7, 7, 7, 7, 7, 7, 7];
var effortSummaryRowNumber = [10, 10, 10, 10, 33, 33, 33, 33, 56, 56];
var effortSummaryColumnNumber = [3, 7, 11, 15, 3, 7, 11, 15, 3, 7];

var sprintNumCellRowCol = [3,2]
var sprintStartDateCellRowCol = [3, 6]
var sprintEndDateCellRowCol = [4, 6]
 
function onOpen()
{
  var spreadSheet = SpreadsheetApp.getActiveSpreadsheet();
  var menuEntries = [{name:"Mail Preferences",functionName:"showMailSettings"},
                    {name:"Send Mail Now",functionName:"sendMail"},
                     {name:"Send Reminder Now",functionName:"sendReminder"}];
  
  spreadSheet.addMenu("Mail", menuEntries);
  
}

function onInstall(e) {
  onOpen(e);
}

function showMailSettings()
{
  var mailSettingUI = HtmlService.createHtmlOutputFromFile("MailSettings.html").setTitle("Mail Settings - GM HMI TEAM")
              .setSandboxMode(HtmlService.SandboxMode.IFRAME);
  SpreadsheetApp.getUi().showSidebar(mailSettingUI);
}

function getMailSettings()
{
  var scriptProperties = PropertiesService.getScriptProperties();
  
  var recipientsTO = scriptProperties.getProperty(RECIPIENTS_TO_KEY);
  var recipientsCC = scriptProperties.getProperty(RECIPIENTS_CC_KEY);
  var mailSubject = scriptProperties.getProperty(SUBJECT_KEY);
  var mailMessage = scriptProperties.getProperty(MESSAGE_KEY);
  var mailTriggerFreq = scriptProperties.getProperty(MAIL_FREQ_KEY);
  var recipientsReminder = scriptProperties.getProperty(REMINDER_RECIPIENTS_KEY);
  var reminderSubject = scriptProperties.getProperty(REMINDER_SUBJECT_KEY);
  var reminderMessage = scriptProperties.getProperty(REMINDER_MESSAGE_KEY);

  var mailProperties = {recipientsTO:recipientsTO,recipientsCC:recipientsCC,mailSubject:mailSubject,mailMessage:mailMessage,mailTriggerFreq:mailTriggerFreq,recipientsReminder:recipientsReminder,reminderSubject:reminderSubject,reminderMessage:reminderMessage};
  return mailProperties;
}

function saveMailSettings(formObject)
{
  Logger.log("in saveMailSettings");
  var recipientsTO = formObject.recipientsTO;
  var recipientsCC = formObject.recipientsCC;
  var mailSubject = formObject.mailSubject;
  var mailMessage = formObject.mailMessage;
  var mailTriggerFreq = formObject.mailTriggerFreq;  
  var recipientsReminder = formObject.recipientsReminder;
  var reminderSubject = formObject.reminderSubject;
  var reminderMessage = formObject.reminderMessage;
  
  var scriptProperties = PropertiesService.getScriptProperties();

  scriptProperties.setProperty(RECIPIENTS_TO_KEY,recipientsTO);
  scriptProperties.setProperty(RECIPIENTS_CC_KEY,recipientsCC);
  scriptProperties.setProperty(SUBJECT_KEY,mailSubject);
  scriptProperties.setProperty(MESSAGE_KEY,mailMessage); 
  scriptProperties.setProperty(MAIL_FREQ_KEY,mailTriggerFreq); 
  
  scriptProperties.setProperty(REMINDER_SUBJECT_KEY,reminderSubject);
  scriptProperties.setProperty(REMINDER_MESSAGE_KEY,reminderMessage);
  scriptProperties.setProperty(REMINDER_RECIPIENTS_KEY,recipientsReminder);
}

function showMessagePopup(title,errorMessage)
{
  SpreadsheetApp.getActiveSpreadsheet().toast(errorMessage, title, 4);
}

function getAsPdf(spreadsheetId) {
  var commonOptionsSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("CommonOptions");
  if (commonOptionsSheet != null) {
    commonOptionsSheet.hideSheet();
  }
  
  var historySheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("History");
  if (historySheet != null) {
    historySheet.hideSheet();
  }
  
  var file = DriveApp.getFileById(spreadsheetId);
  var url = "https://docs.google.com/spreadsheets/d/"+spreadsheetId+"/export?&exportFormat=pdf&sheetnames=true&portrait=true&gridlines=false&pagenumbers=true";
  var token = ScriptApp.getOAuthToken();
  var response = UrlFetchApp.fetch(url, {
    headers: {
      'Authorization': 'Bearer ' +  token
    }
  });
  if (commonOptionsSheet != null) {
    commonOptionsSheet.showSheet();
  }
  if (historySheet != null) {
    historySheet.showSheet();
  }
  return response.getBlob();
}

function getAsXls(spreadsheetId) {
  var file = DriveApp.getFileById(spreadsheetId);
  var url = "https://docs.google.com/spreadsheets/d/"+spreadsheetId+"/export?&exportFormat=xlsx";
  var token = ScriptApp.getOAuthToken();
  var response = UrlFetchApp.fetch(url, {
    headers: {
      'Authorization': 'Bearer ' +  token
    }
  });
  return response.getBlob();
}

function sendMail()
{
  var sprintReportSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("SprintReport");
  var sprintNo = sprintReportSheet.getRange(sprintNumCellRowCol[0], sprintNumCellRowCol[1]).getValue();
  var sprintStartDate = sprintReportSheet.getRange(sprintStartDateCellRowCol[0], sprintStartDateCellRowCol[1]).getValue();
  var sprintStartDateStr = Utilities.formatDate(sprintStartDate, "GMT+05:30", "dd MMM yyyy");
  var sprintEndDate = sprintReportSheet.getRange(sprintEndDateCellRowCol[0], sprintEndDateCellRowCol[1]).getValue();
  var sprintEndDateStr = Utilities.formatDate(sprintEndDate, "GMT+05:30", "dd MMM yyyy");
  
  var scriptProperties = PropertiesService.getScriptProperties();
  var mailSubject = scriptProperties.getProperty(SUBJECT_KEY)+" "+sprintNo+ " ("+ sprintStartDateStr +" - "+ sprintEndDateStr +")";
  var mailMessage = scriptProperties.getProperty(MESSAGE_KEY);
  var recipientsTO = scriptProperties.getProperty(RECIPIENTS_TO_KEY);
  var recipientsCC = scriptProperties.getProperty(RECIPIENTS_CC_KEY);  
  
  var attachementDocPdf = getAsPdf(SpreadsheetApp.getActiveSpreadsheet().getId());
  var attachementDocXls = getAsXls(SpreadsheetApp.getActiveSpreadsheet().getId());
 
  MailApp.sendEmail({to:recipientsTO,cc:recipientsCC,subject:mailSubject,htmlBody:mailMessage,attachments:[attachementDocPdf,attachementDocXls],name:'Effort Report Tool'});
  
  clearData();
}

function sendMailTest()
{
  var sprintReportSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("SprintReport");
  var sprintNo = sprintReportSheet.getRange(sprintNumCellRowCol[0], sprintNumCellRowCol[1]).getValue();
  var sprintStartDate = sprintReportSheet.getRange(sprintStartDateCellRowCol[0], sprintStartDateCellRowCol[1]).getValue();
  var sprintStartDateStr = Utilities.formatDate(sprintStartDate, "GMT+05:30", "dd MMM yyyy");
  var sprintEndDate = sprintReportSheet.getRange(sprintEndDateCellRowCol[0], sprintEndDateCellRowCol[1]).getValue();
  var sprintEndDateStr = Utilities.formatDate(sprintEndDate, "GMT+05:30", "dd MMM yyyy");
  
  var scriptProperties = PropertiesService.getScriptProperties();
  var mailSubject = scriptProperties.getProperty(SUBJECT_KEY)+" "+sprintNo+ " ("+ sprintStartDateStr +" - "+ sprintEndDateStr +")";
  var mailMessage = scriptProperties.getProperty(MESSAGE_KEY);
  var recipientsTO = "elangovan.manickam@harman.com"
  var recipientsCC = ""  
  
  var attachementDocPdf = getAsPdf(SpreadsheetApp.getActiveSpreadsheet().getId());
  var attachementDocXls = getAsXls(SpreadsheetApp.getActiveSpreadsheet().getId());
 
  MailApp.sendEmail({to:recipientsTO,cc:recipientsCC,subject:mailSubject,htmlBody:mailMessage,attachments:[attachementDocPdf,attachementDocXls],name:'Effort Report Tool'});
}

function sendReminder()
{
  var sprintReportSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("SprintReport");
  var sprintNo = sprintReportSheet.getRange(sprintNumCellRowCol[0], sprintNumCellRowCol[1]).getValue();
  var sprintStartDate = sprintReportSheet.getRange(sprintStartDateCellRowCol[0], sprintStartDateCellRowCol[1]).getValue();
  var sprintStartDateStr = Utilities.formatDate(sprintStartDate, "GMT+05:30", "dd MMM yyyy");
  var sprintEndDate = sprintReportSheet.getRange(sprintEndDateCellRowCol[0], sprintEndDateCellRowCol[1]).getValue();
  var sprintEndDateStr = Utilities.formatDate(sprintEndDate, "GMT+05:30", "dd MMM yyyy");
  
  var scriptProperties = PropertiesService.getScriptProperties();
  var reminderSubject = scriptProperties.getProperty(REMINDER_SUBJECT_KEY)+" "+sprintNo+ " ("+ sprintStartDateStr +" - "+ sprintEndDateStr +")";
  var reminderMessage = scriptProperties.getProperty(REMINDER_MESSAGE_KEY);
  var recipientsReiminder = scriptProperties.getProperty(REMINDER_RECIPIENTS_KEY);
    
  MailApp.sendEmail({to:recipientsReiminder,cc:"",subject:reminderSubject,htmlBody:reminderMessage,attachments:[],name:'Effort Report Tool - Reminder'});
}

function sendReminderTest()
{
  var sprintReportSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("SprintReport");
  var sprintNo = sprintReportSheet.getRange(sprintNumCellRowCol[0], sprintNumCellRowCol[1]).getValue();
  var sprintStartDate = sprintReportSheet.getRange(sprintStartDateCellRowCol[0], sprintStartDateCellRowCol[1]).getValue();
  var sprintStartDateStr = Utilities.formatDate(sprintStartDate, "GMT+05:30", "dd MMM yyyy");
  var sprintEndDate = sprintReportSheet.getRange(sprintEndDateCellRowCol[0], sprintEndDateCellRowCol[1]).getValue();
  var sprintEndDateStr = Utilities.formatDate(sprintEndDate, "GMT+05:30", "dd MMM yyyy");
  
  var scriptProperties = PropertiesService.getScriptProperties();
  var reminderSubject = scriptProperties.getProperty(REMINDER_SUBJECT_KEY)+" "+sprintNo+ " ("+ sprintStartDateStr +" - "+ sprintEndDateStr +")";
  var reminderMessage = scriptProperties.getProperty(REMINDER_MESSAGE_KEY);
  var recipientsReiminder = "elangovan.manickam@harman.com"
    
  MailApp.sendEmail({to:recipientsReiminder,cc:"",subject:reminderSubject,htmlBody:reminderMessage,attachments:[],name:'Effort Report Tool - Reminder'});
}

function handleEditing(e)
{
  try{
     Logger.log("handleEditing called"); 
    var activeSpreadSheet = SpreadsheetApp.getActiveSpreadsheet();
    var sprintReportSheet = activeSpreadSheet.getSheetByName("SprintReport");
    var chartSheet = activeSpreadSheet.getSheetByName("Chart");
    var activeSheetName = SpreadsheetApp.getActiveSheet().getName();
    
    var rowNumber = e.range.getRow()
    var columnNumber = e.range.getColumn()    
   
    if(activeSheetName == "SprintReport" && rowNumber == sprintEndDateCellRowCol[0] && columnNumber == sprintEndDateCellRowCol[1])
    {
      var sprintEndDate = sprintReportSheet.getRange(sprintEndDateCellRowCol[0], sprintEndDateCellRowCol[1]).getValue();
      createOrReplaceEmailTriggerTimer(sprintEndDate)
    } 
    else 
    {      
      var indexOfSheetInUpdateList = sheetsToUpdateEffortInChart.indexOf(activeSheetName);
      if(indexOfSheetInUpdateList != -1 && effortUpdatedColumnNumber[indexOfSheetInUpdateList] == columnNumber)
      {
        var totalEffort = sprintReportSheet.getRange(effortSummaryRowNumber[indexOfSheetInUpdateList], effortSummaryColumnNumber[indexOfSheetInUpdateList]).getDisplayValue();
        
        var chart = chartSheet.getCharts()[indexOfSheetInUpdateList];
        chart = chart.modify()
        .setOption('title', activeSheetName+" (Total Effort:  "+ totalEffort +")")
        .build();
        chartSheet.updateChart(chart);
      }
    }  
  }
  catch (ex) {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    ss.toast("error: ex: "+ex);
  }
}

function createOrReplaceEmailTriggerTimer(dateToSendEmail)
{
  try{
    var allTriggers = ScriptApp.getProjectTriggers();
    for (var i = 0; i < allTriggers.length; i++) 
    {
      if(allTriggers[i].getHandlerFunction() == "sendMail")
      {
        ScriptApp.deleteTrigger(allTriggers[i]);
      }
    }
   
    ScriptApp.newTrigger('sendMail')
    .timeBased()
    .at(new Date(dateToSendEmail.getTime() + (1000 * 60 * 60 * 23)))
    .create();
  }
  catch (ex) {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    ss.toast("error: ex: "+ex);
  }
}

function clearData()
{
  var sheets = SpreadsheetApp.getActiveSpreadsheet().getSheets();  
  var historySheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("History");
  var sprintReportSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("SprintReport");
  var sprintNo = sprintReportSheet.getRange(sprintNumCellRowCol[0], sprintNumCellRowCol[1]).getValue();
  
  for (var i = 0 ; i < sheets.length ; i++ )
  {
    if(sheetsNotToClear.indexOf(sheets[i].getName()) == -1)
    {
      copyData(sheets[i], historySheet, sprintNo);
      sheets[i].getDataRange().offset(1,0).clearContent();
    }
  }
}

function copyData(sourceSheet, destSheet, sprintNo)
{
  var sourceRange = sourceSheet.getDataRange().offset(1,0);
  var sourceData = sourceRange.getValues();
  destSheet.appendRow([sourceSheet.getSheetName(),"Sprint: "+sprintNo]);
  var resourceNameRowRange = destSheet.getRange(destSheet.getLastRow(),1,1,sourceData[0].length);
  resourceNameRowRange.setBackground("#b30059").setFontColor("white");
  destSheet.getRange(destSheet.getLastRow()+1, 1, sourceData.length, sourceData[0].length).setValues(sourceData);
}