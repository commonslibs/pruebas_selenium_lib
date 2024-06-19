package es.juntadeandalucia.agapa.pruebasSelenium.listeners;

import com.automation.remarks.testng.UniversalVideoListener;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest;
import org.testng.ITestResult;


public class VideoListener extends UniversalVideoListener {

   @Override
   public void onTestStart(ITestResult result) {
      if (!VariablesGlobalesTest.IS_REMOTO) {
         super.onTestStart(result);
      }
   }

   @Override
   public void onTestSuccess(ITestResult result) {
      if (!VariablesGlobalesTest.IS_REMOTO) {
         super.onTestSuccess(result);
      }
   }

   @Override
   public void onTestFailure(ITestResult result) {
      if (!VariablesGlobalesTest.IS_REMOTO) {
         this.onTestSuccess(result);
      }
   }

}
