import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.Status;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import org.openqa.selenium.io.FileHandler;

public class FunkcionalnostSajtaKOmpresor {
    WebDriver driver;
    ExtentReports extent;
    ExtentTest test;
    ExtentSparkReporter reporter;

    @BeforeTest
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));


        String path = System.getProperty("user.dir") + "\\Reports2\\Kompresor.html";
        reporter = new ExtentSparkReporter(path);
        extent = new ExtentReports();
        extent.attachReporter(reporter);
        extent.setSystemInfo("Tester", "Nemanja Nikitovic");
        reporter.config().setReportName("Rezultati Testiranja Funkcionalnosti");
        reporter.config().setDocumentTitle("Web testiranje Kompresor Sajta");


        driver.get("https://kompresoring.com/kontakt/");
        driver.manage().window().maximize();
    }

    @Test
    public void testContactForm() {

        File screenshotsDir = new File("Reports2/screenshots");
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs();
        }

        String[][] testData = {
                {"NemanjaNikitovic", "1234567890", "test@example.com", "Testiranje1"}, // Neispravno ime
                {"Nemanja Nikitovic", "123ABC7890", "test@example.com", "Testiranje2"}, // Neispravan telefon
                {"Nemanja Nikitovic", "1234567890", "testexample.com", "Testiranje3"}, // Neispravan email
                {"Nemanja Nikitovic", "0636465646", "nemanjanikit123@gmail.com", "Test je uspesan"}  // Ispravan unos
        };

        for (int i = 0; i < testData.length; i++) {
            String name = testData[i][0];
            String phone = testData[i][1];
            String email = testData[i][2];
            String message = testData[i][3];

            test = extent.createTest("Test kontakt formulara - Pokusaj " + (i + 1));


            try {
                driver.findElement(By.id("form-field-name")).clear();
                driver.findElement(By.id("form-field-name")).sendKeys(name);
                driver.findElement(By.id("form-field-field_b773a64")).clear();
                driver.findElement(By.id("form-field-field_b773a64")).sendKeys(phone);
                driver.findElement(By.id("form-field-email")).clear();
                driver.findElement(By.id("form-field-email")).sendKeys(email);
                driver.findElement(By.id("form-field-message")).clear();
                driver.findElement(By.id("form-field-message")).sendKeys(message);
                driver.findElement(By.cssSelector("button.elementor-button.elementor-size-sm")).click();


                boolean isError = false;
                try {
                    if (driver.findElement(By.cssSelector(".elementor-message.elementor-message-danger")).isDisplayed()) {
                        isError = true;
                    }
                } catch (NoSuchElementException e) {
                    isError = false;
                }

                if (isError) {
                    test.log(Status.FAIL, "Forma nije uspešno poslata. Pojavila se greska.");

                    // Take a screenshot
                    File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                    try {
                        FileHandler.copy(screenshot, new File("Reports2/screenshots/" + "Attempt_" + (i + 1) + "_error.png"));
                        test.addScreenCaptureFromPath("screenshots/" + "Attempt_" + (i + 1) + "_error.png");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Optionally check if the form was successfully submitted
                    boolean isSuccess = false;
                    try {
                        isSuccess = driver.findElement(By.cssSelector(".success-message")).isDisplayed();
                    } catch (NoSuchElementException e) {
                        isSuccess = false;
                    }

                    if (isSuccess) {
                        test.log(Status.PASS, "Forma je uspesno poslata.");
                    } else {
                        test.log(Status.FAIL, "Forma nije uspesno poslata.");
                    }
                }
            } catch (Exception e) {
                test.log(Status.FAIL, "Greska tokom testiranja: " + e.getMessage());
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                try {
                    FileHandler.copy(screenshot, new File("Reports2/screenshots/" + "Attempt_" + (i + 1) + "_error.png"));
                    test.addScreenCaptureFromPath("screenshots/" + "Attempt_" + (i + 1) + "_error.png"); // Add screenshot to report
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {

            test.log(Status.FAIL, "Test nije uspeo: " + result.getThrowable());


            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String screenshotPath = "Reports2/screenshots/" + result.getName() + ".png";

            try {

                File screenshotDir = new File("Reports2/screenshots/");
                if (!screenshotDir.exists()) {
                    screenshotDir.mkdirs();
                }


                FileHandler.copy(screenshot, new File(screenshotPath));
                test.addScreenCaptureFromPath(screenshotPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            test.log(Status.PASS, "Test je uspesno prosao.");
        }
    }


    @AfterTest
    public void close() {
        driver.quit();
        extent.flush();
    }
}


