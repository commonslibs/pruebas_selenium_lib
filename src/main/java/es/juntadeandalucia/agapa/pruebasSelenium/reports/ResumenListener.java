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

   private String directorioDeSalida = "";

   @Override
   public void onFinish(ITestContext context) {
      if (context.getPassedTests().getAllResults().size() > 0) {
         this.escribirTraza("Test ejecutados:");
         context.getPassedTests().getAllResults().forEach(result -> {
            this.escribirTraza(this.nombreCasoYCiclo(result));
         });
      }

      if (context.getFailedTests().getAllResults().size() > 0) {
         this.escribirTraza("Test fallidos");
         context.getFailedTests().getAllResults().forEach(result -> {
            this.escribirTraza(this.nombreCasoYCiclo(result));
         });
      }

      this.escribirTraza("Tests completados: " + context.getEndDate().toString());
   }

   @Override
   public void onStart(ITestContext arg0) {
      this.escribirTraza("Empiezan tests regresión: " + arg0.getStartDate().toString());
      this.directorioDeSalida = arg0.getOutputDirectory();
   }

   @Override
   public void onTestFailure(ITestResult arg0) {
      this.escribirTraza("Test: " + arg0.getMethod().getDescription());
      this.escribirTraza("Clase: " + arg0.getMethod().getInstance().getClass().getSimpleName());
      this.escribirTraza("Método: " + arg0.getMethod().getMethodName());

      Object testClass = arg0.getInstance();
      WebDriver driver = ((TestSeleniumAbstracto) testClass).getDriver();
      // this.takeSnapShot(driver, this.nombreCasoYCiclo(arg0));
   }

   @Override
   public void onTestSkipped(ITestResult arg0) {
      this.escribirTraza("Test saltados: " + this.nombreCasoYCiclo(arg0));
   }

   @Override
   public void onTestStart(ITestResult arg0) {
      this.escribirTraza("Ejecutando test: " + this.nombreCasoYCiclo(arg0));
   }

   @Override
   public void onTestSuccess(ITestResult arg0) {
      long timeTaken = ((arg0.getEndMillis() - arg0.getStartMillis())) / 1000;
      this.escribirTraza("Test: " + this.nombreCasoYCiclo(arg0) + " -> Tiempo empleado: " + timeTaken + " s");
   }

   protected String nombreCasoYCiclo(ITestResult arg0) {
      return arg0.getTestClass().getRealClass().getSimpleName() + "." + arg0.getName();
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
   private void takeSnapShot(WebDriver driver, String fichero) {
      Assert.notNull(driver, "Driver no puede ser nulo");
      String fileWithPath = this.directorioDeSalida + "/" + fichero + ".png";
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