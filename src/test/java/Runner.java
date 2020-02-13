import com.codeborne.selenide.SelenideElement;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.*;

import io.restassured.RestAssured.*;

public class Runner {
    static WebDriver driver;
    static RequestSpecification requestSpec;
    static ResponseSpecification responseSpec;
    @BeforeClass
    public static void initDriver() {
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/src/test/resources/chromedriver");
        System.setProperty("selenide.browser", "Chrome");
        open();
        driver = getWebDriver();
        requestSpec = given()
                .baseUri("https://reqres.in/")
                .contentType("application/json");
        responseSpec = expect()
                .contentType("application/json")
                .statusCode(200);
    }

    @Test
    public void testCheckOpenRuIsOnTop10GoogleSearch() {
        driver.navigate().to("http://google.com");
        $(By.xpath("//input[@title= \"Поиск\"]")).setValue("Открытие").pressEnter();
        $$(By.tagName("cite")).find(text("www.open.ru")).shouldBe(exist).click();
        Assert.assertTrue(driver.getCurrentUrl().contains("open.ru"), "URL site is false");
    }
    @Test
    public void testCheckExchangeOnMainPage() throws InterruptedException {
        driver.navigate().to("http://open.ru");
        SelenideElement course = $(By.xpath("//div[contains(@class, 'exchange') and contains(@class, 'card') and descendant::h2[contains(text(), 'Курс')]]"));
        course.shouldBe(visible);
        executeJavaScript("arguments[0].scrollIntoView();", course);
        Assert.assertTrue(course.getText().contains("Курс обмена в интернет-банке"));
        List<String> list = course.$$(By.tagName("tr")).texts();
        for (String string: list
        ) {
            if(string.contains("USD")) {
                String val1 = string.split("[(USD|EUR)\\s\\/]+")[1];
                String val2 = string.split("[(USD|EUR)\\s\\/]+")[2];
                Assert.assertTrue(Double.valueOf(val1.replace(",", ".")).compareTo(Double.valueOf(val2.replace(",", ".")))<0, "USD exchange is not valid "  + val1 + " is greater than" + val2);
            }
            if(string.contains("EUR")) {
                String val1 = string.split("[(EUR)\\s\\/]+")[1];
                String val2 = string.split("[(EUR)\\s\\/]+")[2];
                Assert.assertTrue(Double.valueOf(val1.replace(",", ".")).compareTo(Double.valueOf(val2.replace(",", ".")))<0, "EUR exchange is not valid" + val1 + " is greater than" + val2);
            }
        }
    }
    @Test
    public void testReatApiGet(){
        ValidatableResponse response =
                requestSpec.when()
                .get("api/users?page=2")
                .then()
                .assertThat()
                .spec(responseSpec);
        JsonPath jsonPathValidator = response.extract().jsonPath();
        Assert.assertTrue(jsonPathValidator.get("page")!=null, "Field page is null");
        Assert.assertTrue(jsonPathValidator.get("per_page")!=null, "Field per_page is null");
        Assert.assertTrue(jsonPathValidator.get("total")!=null, "Field total is null");
        Assert.assertTrue(jsonPathValidator.get("total_pages")!=null, "Field total_pages is null");
        Assert.assertTrue(jsonPathValidator.get("data")!=null, "Field data is null");
        List<HashMap> list = jsonPathValidator.getList("data");
        for(HashMap item : list)
        {
            Assert.assertTrue(item.get("last_name")!=null, "Field last_name is null");
            Assert.assertTrue(item.get("first_name")!=null, "Field first_name is null");
            Assert.assertTrue(item.get("id")!=null, "Field id is null");
            Assert.assertTrue(item.get("email")!=null, "Field email is null");
            Assert.assertTrue(item.get("avatar")!=null, "Field avatar is null");
        }
    }
    @Test
    public void testRestApiPost(){
        ValidatableResponse response =
                requestSpec.when()
                        .body("{\n" +
                                "    \"name\": \"morpheus\",\n" +
                                "    \"job\": \"leader\"\n" +
                                "}")
                        .post("api/users")
                        .then()
                        .assertThat()
                        .statusCode(201);
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd");
        JsonPath jsonPathValidator = response.extract().jsonPath();
        Assert.assertTrue(jsonPathValidator.get("name").toString().equals("morpheus"), "Response Field is not valid");
        Assert.assertTrue(jsonPathValidator.get("job").toString().equals("leader"), "Response Field is not valid");
        Assert.assertTrue(jsonPathValidator.get("createdAt").toString().contains(formatForDateNow.format(dateNow)), "Response Field is not valid");
        System.out.println(jsonPathValidator.get().toString());
    }
    @AfterClass
    public void shutDown(){
        driver.close();
    }
}