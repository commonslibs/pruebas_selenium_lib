package es.juntadeandalucia.agapa.pruebasSelenium.webdriver;

import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.WebElementWrapperPrimeFace;


public class WebDriverFactoryPrimeFace extends WebDriverFactory {

   private static WebElementWrapperPrimeFace webElementWrapper;

   public static WebElementWrapperPrimeFace getWebElementWrapper() {
      if (WebDriverFactoryPrimeFace.webElementWrapper == null) {
         WebDriverFactoryPrimeFace.webElementWrapper = new WebElementWrapperPrimeFace();
      }
      return WebDriverFactoryPrimeFace.webElementWrapper;
   }

}
