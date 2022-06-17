package es.juntadeandalucia.agapa.pruebasSelenium.reports;

import es.juntadeandalucia.agapa.pruebasSelenium.suite.TestSeleniumAbstracto;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.util.Assert;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;


@Slf4j
public class ResumenListener implements ITestListener {

   @Override
   public void onFinish(ITestContext context) {
      if (context.getPassedTests().getAllResults().size() > 0) {
         this.escribirTraza("Test ejecutados:");
         context.getPassedTests().getAllResults().forEach(result -> {
            this.escribirTraza(result.getName());
         });
      }

      if (context.getFailedTests().getAllResults().size() > 0) {
         this.escribirTraza("Test fallidos");
         context.getFailedTests().getAllResults().forEach(result -> {
            this.escribirTraza(result.getName());
         });
      }

      this.escribirTraza("Tests completados: " + context.getEndDate().toString());
   }

   @Override
   public void onStart(ITestContext arg0) {
      this.escribirTraza("Empiezan tests regresión: " + arg0.getStartDate().toString());
   }

   @Override
   public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void onTestFailure(ITestResult arg0) {
      this.escribirTraza("Fallido: " + arg0.getName());
      this.escribirTraza("getTestName: " + arg0.getTestName());
      this.escribirTraza("getTestClass : " + arg0.getClass());
      this.escribirTraza("getMethod : " + arg0.getMethod());

      Object testClass = arg0.getInstance();
      WebDriver driver = ((TestSeleniumAbstracto) testClass).getDriver();
      this.takeSnapShot(driver);
   }

   @Override
   public void onTestSkipped(ITestResult arg0) {
      this.escribirTraza("Test saltados: " + arg0.getName());
   }

   @Override
   public void onTestStart(ITestResult arg0) {
      this.escribirTraza("Ejecutando test: " + arg0.getName());
   }

   @Override
   public void onTestSuccess(ITestResult arg0) {
      long timeTaken = ((arg0.getEndMillis() - arg0.getStartMillis()));
      this.escribirTraza("Test: " + arg0.getName() + " Tiempo empleado:" + timeTaken + " ms");
   }

   /**
    * Escribe en consola y en la seccion ReporterOutput del informe html
    */
   private void escribirTraza(String msg) {
      log.info(msg);
      Reporter.log(msg);
   }

   /**
    * Toma una foto del navegador y la guarda en test-output como snapshot.png
    */
   private void takeSnapShot(WebDriver driver) {
      Assert.notNull(driver, "Driver no puede ser nulo");
      String fileWithPath = "test-output//snapshot.png";
      TakesScreenshot scrShot = ((TakesScreenshot) driver);
      File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
      File DestFile = new File(fileWithPath);
      try {
         FileUtils.copyFile(SrcFile, DestFile);
      }
      catch (IOException e) {
         log.error("No se ha podido guardar la foto", e);
      }
   }
}