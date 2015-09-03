package it.angelic.soulissclient.test;

import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import junit.framework.TestCase;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.SoulissDataService;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.HalfFloatUtils;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.typicals.SoulissTypical51AnalogueSensor;

/**
 * Created by pegoraro on 02/09/2015.
 */
public class SoulissTestPersistence extends AndroidTestCase {
    private SoulissDBHelper db;
    private static final short fakeNodeId = 1;
    private SoulissPreferenceHelper opzioni;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new SoulissDBHelper(context);
        opzioni = new SoulissPreferenceHelper(context);
        db.open();
    }

    @Override
    public void tearDown() throws Exception {
        db.close();
        super.tearDown();
        Log.i(Constants.TAG, "tearDown test DB");
    }

    @Override
    protected void runTest() throws Throwable {
        testAddNode();
        testAddSensorTypical();
    }

    public void testAddNode(){
        SoulissNode testNode = new SoulissNode((short)fakeNodeId);
        db.createOrUpdateNode(testNode);
        assertEquals(db.countNodes(), 1);
        // Here i have my new database wich is not connected to the standard database of the App
    }

    public void testAddSensorTypical(){
        SoulissTypical51AnalogueSensor testTypical = new SoulissTypical51AnalogueSensor(opzioni);
        testTypical.getTypicalDTO().setNodeId(fakeNodeId);
        testTypical.getTypicalDTO().setSlot((short) 1);
        SoulissNode father = db.getSoulissNode(fakeNodeId);
        testTypical.setParentNode(father);
        testTypical.getTypicalDTO().persist();

        assertEquals(db.countTypicals(), 1);
        // Here i have my new database wich is not connected to the standard database of the App
    }
}
