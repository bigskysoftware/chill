package chill.m8.scratch;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Scratch {

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/home/carson/Projects/bigskysoftware/chill-project/bin/drivers/chromedriver-93");
        ChromeOptions options = new ChromeOptions();
        //options.setHeadless(true);
        ChromeDriver driver = new ChromeDriver(options);
        driver.get("https://google.com");
        System.out.println(driver.getCurrentUrl());
        driver.findElement(By.name("btnK")).click();
    }
}
