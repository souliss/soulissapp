package it.angelic.soulissclient.test;

import android.os.Environment;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import java.io.File;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.ExportDatabaseCSVTask;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical51AnalogueSensor;

/**
 * Created by shine@angelic.it on 02/09/2015.
 */
public class SoulissTestExport extends AndroidTestCase {
    private static final short fakeNodeId = 1;
    private static final short fakeSlotId = 1;
    private SoulissDBHelper db;
    private SoulissPreferenceHelper opzioni;

    protected void addFakeNode() {
        SoulissNode testNode = new SoulissNode(fakeNodeId);
        // Here i have my new database wich is not connected to the standard database of the App
        db.createOrUpdateNode(testNode);
        assertEquals(1, db.countNodes());
        // Here i have my new database wich is not connected to the standard database of the App
    }

    protected void addFakeLight() {
        SoulissTypical11DigitalOutput testTypical = new SoulissTypical11DigitalOutput(opzioni);
        testTypical.getTypicalDTO().setTypical(Constants.Typicals.Souliss_T11);
        testTypical.getTypicalDTO().setNodeId(fakeNodeId);
        testTypical.getTypicalDTO().setSlot(fakeSlotId);
        SoulissNode father = db.getSoulissNode(fakeNodeId);
        testTypical.setParentNode(father);

        assertEquals(1, testTypical.getTypicalDTO().persist());
        // Here i have my new database wich is not connected to the standard database of the App
    }

    protected void addFakeSensor() {
        SoulissTypical51AnalogueSensor testTypical = new SoulissTypical51AnalogueSensor(opzioni);
        testTypical.getTypicalDTO().setTypical(Constants.Typicals.Souliss_T51);
        testTypical.getTypicalDTO().setNodeId(fakeNodeId);
        testTypical.getTypicalDTO().setSlot((short) (fakeSlotId + 1));
        SoulissNode father = db.getSoulissNode(fakeNodeId);
        testTypical.setParentNode(father);
        testTypical.getTypicalDTO().persist();
        //assertEquals(1, );
        // Here i have my new database wich is not connected to the standard database of the App
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new SoulissDBHelper(context);
        opzioni = new SoulissPreferenceHelper(context);
        SoulissDBHelper.open();

        addFakeNode();
        addFakeLight();
        addFakeSensor();
    }


    @Override
    public void tearDown() throws Exception {
        getContext().deleteDatabase(SoulissDB.DATABASE_NAME);

        Log.i(Constants.TAG, "tearDown test DB");
        db.close();
        File exportDir = new File(Environment.getExternalStorageDirectory(), Constants.EXTERNAL_EXP_FOLDER);
        exportDir.deleteOnExit();
        super.tearDown();

    }

    public void testExport() {
        ExportDatabaseCSVTask tas = new ExportDatabaseCSVTask();

        tas.loadContext(getContext());
        tas.execute("");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        File exportDir = new File(Environment.getExternalStorageDirectory(), Constants.EXTERNAL_EXP_FOLDER);

        assertTrue(exportDir.exists());

        assertTrue(exportDir.isDirectory());
        assertTrue(exportDir.listFiles().length > 0);
    }


}
