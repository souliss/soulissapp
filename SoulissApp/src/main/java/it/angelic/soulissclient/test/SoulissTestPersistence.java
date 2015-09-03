package it.angelic.soulissclient.test;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.db.SoulissDBHelper;
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

    protected void testAll() throws Throwable {

        Log.i(Constants.TAG, "runTest()");
        addFakeNode();
        addFakeSensor();
    }

    public void addFakeNode(){
        SoulissNode testNode = new SoulissNode((short)fakeNodeId);
        db.createOrUpdateNode(testNode);
        assertEquals(1, db.countNodes());
        // Here i have my new database wich is not connected to the standard database of the App
    }

    public void addFakeSensor(){
        SoulissTypical51AnalogueSensor testTypical = new SoulissTypical51AnalogueSensor(opzioni);
        testTypical.getTypicalDTO().setNodeId(fakeNodeId);
        testTypical.getTypicalDTO().setSlot((short) 1);
        SoulissNode father = db.getSoulissNode(fakeNodeId);
        testTypical.setParentNode(father);
        testTypical.getTypicalDTO().persist();

        assertEquals( 1, db.countTypicals());
        // Here i have my new database wich is not connected to the standard database of the App
    }


    @Override
    public void tearDown() throws Exception {
        Log.i(Constants.TAG, "tearDown test DB");
        db.close();
        super.tearDown();

    }
}
