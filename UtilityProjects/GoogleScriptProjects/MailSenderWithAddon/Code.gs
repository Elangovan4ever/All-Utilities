//This script is written by Elangovan Manickam. For any queries or modifications, please contact: elangovan4ever@gmail.com

var SUBJECT_KEY='MailSubject';
var MESSAGE_KEY='MailBody';
var RECIPIENTS_TO_KEY='RecipientsTO';
var RECIPIENTS_CC_KEY='RecipientsCC';
var MAIL_FREQ_KEY='MailTiggerFreq';

var REMINDER_SUBJECT_KEY='ReminderSubject';
var REMINDER_MESSAGE_KEY='ReminderBody';
var REMINDER_RECIPIENTS_KEY='RecipientsReminder';
 
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
  var url = "https://docs.google.com/spreadsheets/d/"+spreadsheetId+"/export?&exportFormat=pdf&sheetnames=true&portrait=true&gridlines=true&pagenumbers=true";
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

Date.prototype.getWeek = function()
{
    var onejan = new Date(this.getFullYear(),0,1);
    return Math.ceil((((this - onejan) / 86400000) + onejan.getDay()+1)/7);
} 

function WeekNumber(date, inIsoStringFormat)
{
// Allows this method to be called without arguments to return the
// current week. Shorter version of =WEEKNUMBER(TODAY())
if(arguments.length < 1)
date = new Date();
 
// For a short while when I first started writing this script date cells
// wasn't given as pure js date objects so you had to convert them first. This
// hasn't been an issue for me for ages but some comments on my blog suggests
// that explicitly convertering into a Date instance has helped them. Maybe they're
// using old spreadsheets, maybe not. But I can't foresee that adding this extra
// safeguard should cause any major problems (famous last words).
if(Object.prototype.toString.call(date) !== '[object Date]')
date = new Date(date);
 
var activeSpreadsheet = SpreadsheetApp.getActiveSpreadsheet();
var spreadsheetTimeZone = activeSpreadsheet.getSpreadsheetTimeZone(); 
 
// Google apps will automatically convert the spreadsheet date into the timezone
// under which the script is running, this will revert it to the spreadsheet date and
// at the same time truncate hours, minutes and seconds.
date = new Date( Utilities.formatDate(date, spreadsheetTimeZone, "MMM d, yyyy") );
 
// Get the week day where 1 = Monday and 7 = Sunday
var dayOfWeek = ((date.getDay() + 6) % 7) + 1;
 
// Locate the nearest thursday
date.setDate(date.getDate() + (4 - dayOfWeek));
 
var jan1 = new Date(date.getFullYear(), 0, 1);
 
// Calculate the number of days in between the nearest thursday and januari first of
// the same year as the nearest thursday.
var deltaDays = Math.floor( (date.getTime() - jan1.getTime()) / (86400 * 1000) )
 
var weekNumber = 1 + Math.floor(deltaDays / 7);
 
if(inIsoStringFormat)
return jan1.getFullYear() + "-W" + (weekNumber < 10 ? "0" + weekNumber : weekNumber);
 
return weekNumber;
}

function sendMail()
{
  var sprintReportSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("SprintReport");
  var sprintNo = sprintReportSheet.getRange(3, 2).getValue();
  var sprintStartDate = sprintReportSheet.getRange(3, 6).getValue();
  var sprintStartDateStr = Utilities.formatDate(sprintStartDate, "GMT+05:30", "dd MMM yyyy");
  var sprintEndDate = sprintReportSheet.getRange(4, 6).getValue();
  var sprintEndDateStr = Utilities.formatDate(sprintEndDate, "GMT+05:30", "dd MMM yyyy");
  
  var email = Session.getEffectiveUser();
  
  var scriptProperties = PropertiesService.getScriptProperties();
  var mailSubject = scriptProperties.getProperty(SUBJECT_KEY)+" "+sprintNo+ " ("+ sprintStartDateStr +" - "+ sprintEndDateStr +")";
  var mailMessage = scriptProperties.getProperty(MESSAGE_KEY);
  var recipientsTO = scriptProperties.getProperty(RECIPIENTS_TO_KEY);
  var recipientsCC = scriptProperties.getProperty(RECIPIENTS_CC_KEY);  
  
  var attachementDocPdf = getAsPdf(SpreadsheetApp.getActiveSpreadsheet().getId());
  var attachementDocXls = getAsXls(SpreadsheetApp.getActiveSpreadsheet().getId());
 
  MailApp.sendEmail({to:recipientsTO,cc:recipientsCC,subject:mailSubject,htmlBody:mailMessage,attachments:[attachementDocPdf,attachementDocXls],name:'Effort Report Tool'});
}

function sendReminder()
{
  var sprintReportSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("SprintReport");
  var sprintNo = sprintReportSheet.getRange(3, 2).getValue();
  var sprintStartDate = sprintReportSheet.getRange(3, 6).getValue();
  var sprintStartDateStr = Utilities.formatDate(sprintStartDate, "GMT+05:30", "dd MMM yyyy");
  var sprintEndDate = sprintReportSheet.getRange(4, 6).getValue();
  var sprintEndDateStr = Utilities.formatDate(sprintEndDate, "GMT+05:30", "dd MMM yyyy");
  
  var email = Session.getEffectiveUser();
  
  var scriptProperties = PropertiesService.getScriptProperties();
  var reminderSubject = scriptProperties.getProperty(REMINDER_SUBJECT_KEY)+" "+sprintNo+ " ("+ sprintStartDateStr +" - "+ sprintEndDateStr +")";
  var reminderMessage = scriptProperties.getProperty(REMINDER_MESSAGE_KEY);
  var recipientsReiminder = scriptProperties.getProperty(REMINDER_RECIPIENTS_KEY);
    
  MailApp.sendEmail(recipientsReiminder,reminderSubject, reminderMessage);
}

function deleteTrigger(triggerId) 
{
  // Loop over all triggers.
  var allTriggers = ScriptApp.getProjectTriggers();
  for (var i = 0; i < allTriggers.length; i++) 
  {
    // If the current trigger is the correct one, delete it.
    if (allTriggers[i].getUniqueId() == triggerId) 
    {
      ScriptApp.deleteTrigger(allTriggers[i]);
      break;
    }
  }
} 

function deleteAllTriggers() 
{
  // Loop over all triggers.
  var allTriggers = ScriptApp.getProjectTriggers();
  for (var i = 0; i < allTriggers.length; i++) 
  {
      ScriptApp.deleteTrigger(allTriggers[i]);
  }
}

function setMailTrigger(selectedFreq)
{
    switch(selectedFreq)
    {
       case "none":
            deleteAllTriggers();      
            break;
       case "daily":
            ScriptApp.newTrigger('sendMail')
                     .timeBased()
                     .onWeekDay(ScriptApp.WeekDay.MONDAY)
                     .atHour(9)
                     .create();
             break;
       case "weekly":
              ScriptApp.newTrigger('sendMail')
                     .timeBased()
                     .onWeekDay(ScriptApp.WeekDay.MONDAY)
                     .atHour(9)
                     .create();     
             break;
        case "monthly":
              ScriptApp.newTrigger('sendMail')
                     .timeBased()
                     .onWeekDay(ScriptApp.WeekDay.MONDAY)
                     .atHour(9)
                     .create();     
             break;
       
    }
}

function clearData()
{
  var sheets = SpreadsheetApp.getActiveSpreadsheet().getSheets();  
  var historySheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("History");
  var sprintReportSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("SprintReport");
  var sprintNo = sprintReportSheet.getRange(3, 5).getValue();
  
  for (var i = 0 ; i < sheets.length ; i++ )
  {
    if(sheets[i].getName() != "CommonOptions" && sheets[i].getName() != "Holidays" && sheets[i].getName() != "SprintReport"  && sheets[i].getName() != "History")
    {
      Logger.log("sheets[i].getName(): "+sheets[i].getName());
      copyData(sheets[i], historySheet, sprintNo);
      sheets[i].getDataRange().offset(1,0).clearContent();
    }
  }
}

function clearDataTest(sourceSheet, destSheet)
{
  var sheets = SpreadsheetApp.getActiveSpreadsheet().getSheets();  
  var historySheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("History");
  var sprintReportSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("SprintReport");
  var sprintNo = sprintReportSheet.getRange(3, 5).getValue();
  
  for (var i = 0 ; i < sheets.length ; i++ )
  {
    if(sheets[i].getName() != "CommonOptions" && sheets[i].getName() != "Holidays" && sheets[i].getName() != "SprintReport"  && sheets[i].getName() != "History")
    {
      Logger.log("sheets[i].getName(): "+sheets[i].getName());
      copyData(sheets[i], historySheet, sprintNo);
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