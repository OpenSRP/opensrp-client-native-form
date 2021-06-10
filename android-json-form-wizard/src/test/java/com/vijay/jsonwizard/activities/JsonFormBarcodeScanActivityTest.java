package com.vijay.jsonwizard.activities;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

@Ignore
public class JsonFormBarcodeScanActivityTest extends BaseActivityTest {

    private JsonFormBarcodeScanActivity barcodeScanActivity;
    private ActivityController<JsonFormBarcodeScanActivity> controller;

    @Mock
    private SparseArray<Barcode> barcodeSparseArray;

    @Mock
    private Detector.Detections<Barcode> detections;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = Robolectric.buildActivity(JsonFormBarcodeScanActivity.class).create().start();
        barcodeScanActivity = controller.get();
    }

    @After
    public void tearDown() {
        destroyController();
    }

    private void destroyController() {
        try {
            barcodeScanActivity.finish();
            controller.pause().stop().destroy(); //destroy controller if we can

        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }

        System.gc();
    }

    @Test
    public void testActivityCreatedSuccessfully() {
        Assert.assertNotNull(barcodeScanActivity);
    }

    @Test
    public void testCloseActivitySuccessfully() {
        barcodeScanActivity.closeBarcodeActivity(barcodeSparseArray);
        Assert.assertTrue(barcodeScanActivity.isFinishing());
    }

    @Test
    public void testReceiveDetections() {
        Assert.assertNotNull(detections);
        Mockito.doReturn(barcodeSparseArray).when(detections).getDetectedItems();
        Assert.assertNotNull(barcodeSparseArray);
        Assert.assertEquals(0, barcodeSparseArray.size());
        Whitebox.setInternalState(barcodeSparseArray.size(), 2);
        Assert.assertEquals(2, barcodeSparseArray.size());

        barcodeScanActivity.receiveDetections(detections);
    }
}
