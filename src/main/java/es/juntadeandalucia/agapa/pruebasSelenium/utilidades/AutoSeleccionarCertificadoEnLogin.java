package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface AutoSeleccionarCertificadoEnLogin {

   String value();
}
