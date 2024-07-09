package es.juntadeandalucia.agapa.pruebasSelenium.listeners;

import com.automation.remarks.testng.UniversalVideoListener;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest;
import org.testng.ITestResult;


/**
 * Listener para gestionar los eventos de video.
 *
 * @author AGAPA
 */
public class VideoListener extends UniversalVideoListener {

   /**
    * On test start.
    *
    * @param result
    *           valor para: result
    */
   @Override
   public void onTestStart(ITestResult result) {
      if (!VariablesGlobalesTest.IS_DOCKER) {
         super.onTestStart(result);
      }
   }

   /**
    * On test success.
    *
    * @param result
    *           valor para: result
    */
   @Override
   public void onTestSuccess(ITestResult result) {
      if (!VariablesGlobalesTest.IS_DOCKER) {
         super.onTestSuccess(result);
      }
   }

   /**
    * On test failure.
    *
    * @param result
    *           valor para: result
    */
   @Override
   public void onTestFailure(ITestResult result) {
      if (!VariablesGlobalesTest.IS_DOCKER) {
         this.onTestSuccess(result);
      }
   }

}
