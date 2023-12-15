package es.juntadeandalucia.agapa.pruebasSelenium.reports;

import static java.util.stream.Collectors.toList;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;


@Slf4j
public class InformeListener implements IReporter {
   private static final Logger LOGGER       = LoggerFactory.getLogger(InformeListener.class);

   private static final String ROW_TEMPLATE = "<tr class=\"%s\"><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";

   @Override
   public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      String reportTemplate = this.initReportTemplate();

      final String body = suites.stream().flatMap(this.suiteToResults()).collect(Collectors.joining());

      this.saveReportTemplate(outputDirectory, reportTemplate.replaceFirst("</tbody>", String.format("%s</tbody>", body)));

      this.mergearReports(suites);
   }

   private void mergearReports(List<ISuite> suites) {
      ExtentSparkReporter sparkMergeado = new ExtentSparkReporter(VariablesGlobalesTest.DIRECTORIO_TARGET_SUREFIRE_REPORTS + "PPI.html");
      ExtentReports reportMergeado = new ExtentReports();
      for (ISuite suite : suites) {
         try {
            String directorioOrigenJson = VariablesGlobalesTest.DIRECTORIO_TARGET_SUREFIRE_REPORTS + suite.getName() + "/";
            String ficheroJson = directorioOrigenJson + "/" + suite.getName() + "-Extendido.json";
            reportMergeado.createDomainFromJsonArchive(ficheroJson);

            // Copiar las capturas de pantallas de error
            File sourceDirectory = new File(directorioOrigenJson + VariablesGlobalesTest.DIRECTORIO_CAPTURAS);
            if (sourceDirectory.exists()) {
               File destinationDirectory =
                     new File(VariablesGlobalesTest.DIRECTORIO_TARGET_SUREFIRE_REPORTS + VariablesGlobalesTest.DIRECTORIO_CAPTURAS);
               FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
            }
         }
         catch (Exception e) {
            log.error(e.getLocalizedMessage());
         }
      }
      reportMergeado.attachReporter(sparkMergeado);
      reportMergeado.flush();
   }

   private Function<ISuite, Stream<? extends String>> suiteToResults() {
      return suite -> suite.getResults().entrySet().stream().flatMap(this.resultsToRows(suite));
   }

   private Function<Map.Entry<String, ISuiteResult>, Stream<? extends String>> resultsToRows(ISuite suite) {
      return e -> {
         ITestContext testContext = e.getValue().getTestContext();

         Set<ITestResult> failedTests = testContext.getFailedTests().getAllResults();
         Set<ITestResult> passedTests = testContext.getPassedTests().getAllResults();
         Set<ITestResult> skippedTests = testContext.getSkippedTests().getAllResults();

         String suiteName = suite.getName();

         return Stream.of(failedTests, passedTests, skippedTests)
               .flatMap(results -> this.generateReportRows(e.getKey(), suiteName, results).stream());
      };
   }

   private List<String> generateReportRows(String testName, String suiteName, Set<ITestResult> allTestResults) {
      return allTestResults.stream().map(this.testResultToResultRow(testName, suiteName)).collect(toList());
   }

   private Function<ITestResult, String> testResultToResultRow(String testName, String suiteName) {
      return testResult -> (switch (testResult.getStatus()) {
         case ITestResult.FAILURE -> String.format(ROW_TEMPLATE, "danger", suiteName, testName, testResult.getName(), "FAILED", "NA");
         case ITestResult.SUCCESS -> String.format(ROW_TEMPLATE, "success", suiteName, testName, testResult.getName(), "PASSED",
               String.valueOf(testResult.getEndMillis() - testResult.getStartMillis()));
         case ITestResult.SKIP -> String.format(ROW_TEMPLATE, "warning", suiteName, testName, testResult.getName(), "SKIPPED", "NA");
         default -> "";
      });
   }

   private String initReportTemplate() {
      String template = null;
      byte[] reportTemplate;
      try {
         reportTemplate = Files.readAllBytes(Paths.get("src/test/resources/reports/reportTemplate.html"));
         template = new String(reportTemplate, "UTF-8");
      }
      catch (IOException e) {
         LOGGER.error("Problem initializing template", e);
      }
      return template;
   }

   private void saveReportTemplate(String outputDirectory, String reportTemplate) {
      new File(outputDirectory).mkdirs();
      try {
         PrintWriter reportWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputDirectory, "my-report.html"))));
         reportWriter.println(reportTemplate);
         reportWriter.flush();
         reportWriter.close();
      }
      catch (IOException e) {
         LOGGER.error("Problem saving template", e);
      }
   }
}
