package com.vijay.jsonwizard.comparisons;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.TestConstants;

import org.junit.Assert;
import org.junit.Test;


public class LessThanEqualToComparisonTest extends BaseTest {

    @Test
    public void compareShouldReturnTrueWhenSecondStringsIsAlphabeticallyRankedHigher() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(true, comparison.compare("stringA", TestConstants.TYPE_STRING, "stringB"));

    }

    @Test
    public void compareShouldReturnTrueWhenTheTwoStringsAreAlphabeticallyEqual() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(true, comparison.compare("stringC", TestConstants.TYPE_STRING, "stringC"));

    }

    @Test
    public void compareShouldReturnFalseWhenSecondStringsIsAlphabeticallyRankedLower() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(false, comparison.compare("stringD", TestConstants.TYPE_STRING, "stringC"));

    }

    @Test
    public void compareShouldReturnTrueWhenFirstStringNotSetAndSecondStringsIsDefaultString() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(true, comparison.compare(null, TestConstants.TYPE_STRING, ""));

    }


    @Test
    public void compareShouldReturnTrueWhenSecondNumberIsGreaterThanTheFirst() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(true, comparison.compare("1", TestConstants.TYPE_NUMERIC, "3"));

    }

    @Test
    public void compareShouldReturnFalseWhenSecondNumberIsLessThanTheFirst() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(false, comparison.compare("4", TestConstants.TYPE_NUMERIC, "1"));

    }

    @Test
    public void compareShouldReturnTrueWhenTheNumbersAreEquals() {
        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(true, comparison.compare("2", TestConstants.TYPE_NUMERIC, "2"));
    }

    @Test
    public void compareShouldReturnFalseWhenFirstNumberIsNotSetAndSecondIsDefaultString() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(false, comparison.compare(null, TestConstants.TYPE_NUMERIC, ""));

    }

    @Test
    public void compareShouldReturnFalseWhenFirstNumberNotSetAndSecondSNumberIsLowerThanDefault() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(false, comparison.compare(null, TestConstants.TYPE_NUMERIC, "-1"));

    }


    @Test
    public void compareShouldReturnTrueWhenSecondDateIsLaterThanTheFirst() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(true, comparison.compare("01-01-2000", TestConstants.TYPE_DATE, "01-01-2030"));

    }

    @Test
    public void compareShouldReturnFalseWhenSecondDateIsLaterThanTheFirst() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(false, comparison.compare("02-02-2030", TestConstants.TYPE_DATE, "01-01-2000"));

    }

    @Test
    public void compareShouldReturnTrueWhenTheDatesAreEqual() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(true, comparison.compare("02-02-2030", TestConstants.TYPE_DATE, "02-02-2030"));

    }

    @Test
    public void compareShouldReturnFalseWhenFirstDateIsNotSetAndSecondIsDefaultString() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(false, comparison.compare(null, TestConstants.TYPE_DATE, ""));

    }

    @Test
    public void compareShouldReturnFalseWhenFirstDateNotSetAndSecondDateIsEarlierThanDefault() {

        LessThanEqualToComparison comparison = new LessThanEqualToComparison();
        Assert.assertEquals(false, comparison.compare(null, TestConstants.TYPE_DATE, "01-01-1800"));

    }
}
