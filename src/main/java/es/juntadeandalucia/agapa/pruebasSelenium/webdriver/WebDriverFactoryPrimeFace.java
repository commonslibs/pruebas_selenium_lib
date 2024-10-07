package es.juntadeandalucia.agapa.pruebasSelenium.webdriver;

import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.WebElementWrapperPrimeFace;


/**
 * The Class WebDriverFactoryPrimeFace.
 */
public class WebDriverFactoryPrimeFace extends WebDriverFactory {

   /** The web element wrapper. */
   private static WebElementWrapperPrimeFace webElementWrapper;

   /**
    * Gets the web element wrapper.
    *
    * @return the web element wrapper
    */
   public static WebElementWrapperPrimeFace getWebElementWrapper() {
      if (WebDriverFactoryPrimeFace.webElementWrapper == null) {
         WebDriverFactoryPrimeFace.webElementWrapper = new WebElementWrapperPrimeFace();
      }
      return WebDriverFactoryPrimeFace.webElementWrapper;
   }

}
