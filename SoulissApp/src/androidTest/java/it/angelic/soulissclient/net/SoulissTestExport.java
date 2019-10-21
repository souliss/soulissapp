package it.angelic.soulissclient.net;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import androidx.test.platform.app.InstrumentationRegistry;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.helpers.ExportDatabaseCSVTask;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBOpenHelper;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical51AnalogueSensor;


/**
 * Created by shine@angelic.it on 02/09/2015.
 */
public class SoulissTestExport extends junit.framework.TestCase {
    private static final short fakeNodeId = 1;
    private static final short fakeSlotId = 1;
    private Context context;
    private SoulissDBHelper db;
    private SoulissPreferenceHelper opzioni;

    protected void addFakeLight() {
        SoulissTypical11DigitalOutput testTypical = new SoulissTypical11DigitalOutput(context, opzioni);
        testTypical.getTypicalDTO().setTypical(Constants.Typicals.Souliss_T11);
        testTypical.getTypicalDTO().setNodeId(fakeNodeId);
        testTypical.getTypicalDTO().setSlot(fakeSlotId);
        SoulissNode father = db.getSoulissNode(fakeNodeId);
        testTypical.setParentNode(father);

        assertEquals(1, testTypical.getTypicalDTO().persist());
        // Here i have my new database wich is not connected to the standard database of the App
    }

    protected void addFakeNode() {
        SoulissNode testNode = new SoulissNode(context, fakeNodeId);
        // Here i have my new database wich is not connected to the standard database of the App
        db.createOrUpdateNode(testNode);
        assertEquals(1, db.countNodes());
        // Here i have my new database wich is not connected to the standard database of the App
    }

    protected void addFakeSensor() {
        SoulissTypical51AnalogueSensor testTypical = new SoulissTypical51AnalogueSensor(context, opzioni);
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
        context = InstrumentationRegistry.getInstrumentation().getContext();
        //non va perche` il context e` farlocco, mDatabaseDir nulla

        db = new SoulissDBHelper(context);
        opzioni = new SoulissPreferenceHelper(context);
        SoulissDBHelper.open();

        addFakeNode();
        addFakeLight();
        addFakeSensor();
    }


    @Override
    public void tearDown() throws Exception {
        context.deleteDatabase(SoulissDBOpenHelper.DATABASE_NAME);

        Log.i(Constants.TAG, "tearDown test DB");
        db.close();
        File exportDir = new File(Environment.getExternalStorageDirectory(), Constants.EXTERNAL_EXP_FOLDER);
        exportDir.deleteOnExit();
        super.tearDown();

    }

    public void testExport() {

        ExportDatabaseCSVTask tas = new ExportDatabaseCSVTask();

        tas.loadContext(context);
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
