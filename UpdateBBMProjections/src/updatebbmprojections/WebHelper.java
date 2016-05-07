package updatebbmprojections;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;

public class WebHelper {

  private WebDriver driver;
  private String user;
  private String password;

  public WebHelper(String user, String password) {
    this.user = user;
    this.password = password;
  }

  public boolean downloadExportFile(String exportFileLocation, char type) throws Exception {
    Logger logger = Logger.getLogger("");
// logger.setLevel(Level.SEVERE);
    logger.setLevel(Level.OFF);

    File exportFile = new File(exportFileLocation);
    if(exportFile.exists() && !exportFile.delete()) {
      logger.log(Level.SEVERE, "Please close " + exportFileLocation + ".");
      return false;
// System.exit(-1);
    }
    else if(exportFile.exists()) {
      exportFile.delete();
    }

    URL website = null;
    switch(type) {
      case 'H':
        website = new URL("https://www.fantasypros.com/mlb/projections/ros-hitters.php?export=xls&year=2016");
        break;
      case 'S':
        website = new URL("https://www.fantasypros.com/mlb/projections/ros-pitchers.php?export=xls&year=2016");
        break;
      case 'R':
        website = new URL("https://www.fantasypros.com/mlb/projections/ros-rp.php?export=xls&year=2016");
        break;
    }
    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
    FileOutputStream fos = new FileOutputStream(exportFileLocation);
    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    fos.close();
    return true;
  }

  public boolean importFile(String hitterFile, String spFile, String rpFile) throws Exception {
    Logger logger = Logger.getLogger("");
// logger.setLevel(Level.SEVERE);
    logger.setLevel(Level.OFF);

    driver = new HtmlUnitDriver() {
      @Override
      protected WebClient modifyWebClient(WebClient client) {
        client.addWebWindowListener(new WebWindowListener() {

          @Override
          public void webWindowOpened(WebWindowEvent event) {

          }

          @Override
          public void webWindowContentChanged(WebWindowEvent event) {
            try {
            }
            catch(Exception e) {
              e.printStackTrace();
            }
          }

          @Override
          public void webWindowClosed(WebWindowEvent event) {

          }
        });
        return super.modifyWebClient(client);
      }
    };
    ((HtmlUnitDriver)driver).setJavascriptEnabled(true);
    driver.get("https://baseballmonster.com/Login.aspx");
    driver.manage().window().setSize(new Dimension(0, 0));
    driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

    driver.findElement(By.id("ContentPlaceHolder1_UsernameTextBox")).sendKeys(user);
    driver.findElement(By.id("ContentPlaceHolder1_PasswordTextBox")).sendKeys(password);
    driver.findElement(By.id("ContentPlaceHolder1_LoginButton")).click();

    driver.findElement(By.linkText("Logout"));

    driver.get("https://baseballmonster.com/customprojections.aspx");
    registerAlertHack();
    driver.findElement(By.id("ContentPlaceHolder1_ClearHittersButton")).click();
    driver.findElement(By.id("ContentPlaceHolder1_ClearPitchersButton")).click();

    driver.findElement(By.id("ContentPlaceHolder1_CSVFileUpload")).sendKeys(hitterFile);
    driver.findElement(By.id("ContentPlaceHolder1_UploadCSVButton")).click();

    try {
      Thread.sleep(5000);
    }
    catch(InterruptedException e) {
      e.printStackTrace();
    }

    driver.findElement(By.id("ContentPlaceHolder1_CSVFileUpload")).sendKeys(spFile);
    driver.findElement(By.id("ContentPlaceHolder1_UploadCSVButton")).click();

    try {
      Thread.sleep(5000);
    }
    catch(InterruptedException e) {
      e.printStackTrace();
    }

    driver.findElement(By.id("ContentPlaceHolder1_CSVFileUpload")).sendKeys(rpFile);
    driver.findElement(By.id("ContentPlaceHolder1_UploadCSVButton")).click();

    driver.quit();

    return true;
  }

  private void registerAlertHack() {
    if(driver != null) {
      String js = "if (window.alert.myAlertText == undefined) {window.alert.myAlertText = null;  window.alert = function(msg){ window.alert.myAlertText = msg; };}";
      ((JavascriptExecutor)driver).executeScript(js);
    }

  }

  public boolean jsAlertExists() {
    return getJsAlertText() != null;
  }

  public String getJsAlertText() {
    Object txt = ((JavascriptExecutor)driver).executeScript("return window.alert.myAlertText;");
    return (String)txt;
  }

  public void clickJsAlert() {
    ((JavascriptExecutor)driver).executeScript("window.alert.myAlertText = null;");
  }
}
