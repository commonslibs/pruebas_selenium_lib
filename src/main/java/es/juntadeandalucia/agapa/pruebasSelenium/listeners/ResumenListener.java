package es.juntadeandalucia.agapa.pruebasSelenium.listeners;

import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;


/**
 * Listener para gestionar los eventos de resumen.
 *
 * @author AGAPA
 */
@Slf4j
public class ResumenListener implements ITestListener {

   /**
    * On finish.
    *
    * @param context
    *           valor para: context
    */
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
      if (context.getSkippedTests().getAllResults().size() > 0) {
         this.escribirTraza("Test saltados");
         context.getSkippedTests().getAllResults().forEach(result -> {
            this.escribirTraza(this.nombreCasoYCiclo(result));
         });
      }
      this.escribirTraza("Tests completados: " + context.getEndDate().toString());
   }

   /**
    * On start.
    *
    * @param contexto
    *           valor para: contexto
    */
   @Override
   public void onStart(ITestContext contexto) {
      this.escribirTraza("Empiezan tests: " + contexto.getStartDate().toString());
   }

   /**
    * On test failure.
    *
    * @param resultado
    *           valor para: resultado
    */
   @Override
   public void onTestFailure(ITestResult resultado) {
      this.escribirTraza("Test: " + resultado.getMethod().getDescription());
      this.escribirTraza("Clase: " + resultado.getMethod().getInstance().getClass().getSimpleName());
      this.escribirTraza("Método: " + resultado.getMethod().getMethodName());
   }

   /**
    * On test skipped.
    *
    * @param resultado
    *           valor para: resultado
    */
   @Override
   public void onTestSkipped(ITestResult resultado) {
      this.escribirTraza("Test saltado: " + this.nombreCasoYCiclo(resultado));
   }

   /**
    * On test start.
    *
    * @param resultado
    *           valor para: resultado
    */
   @Override
   public void onTestStart(ITestResult resultado) {
      this.escribirTraza("Iniciando test: " + this.nombreCasoYCiclo(resultado));
   }

   /**
    * On test success.
    *
    * @param resultado
    *           valor para: resultado
    */
   @Override
   public void onTestSuccess(ITestResult resultado) {
      double tiempo = ((resultado.getEndMillis() - resultado.getStartMillis())) / 1000D;
      this.escribirTraza(
            "Test finalizado: " + this.nombreCasoYCiclo(resultado) + " -> Tiempo empleado: " + String.format("%.2f", tiempo) + " segundos");
   }

   /**
    * Nombre caso Y ciclo.
    *
    * @param resultado
    *           valor para: resultado
    * @return string
    */
   protected String nombreCasoYCiclo(ITestResult resultado) {
      return resultado.getTestClass().getRealClass().getSimpleName() + "." + resultado.getName();
   }

   /**
    * Escribe en consola y en la seccion ReporterOutput del informe HTML.
    *
    * @param mensaje
    *           valor para: mensaje
    */
   private void escribirTraza(String mensaje) {
      log.info(mensaje);
      Reporter.log(mensaje);
   }

}