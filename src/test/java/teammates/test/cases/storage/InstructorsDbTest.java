package teammates.test.cases.storage;

import static teammates.common.Common.EOL;
import static teammates.common.FieldValidator.EMAIL_ERROR_MESSAGE;
import static teammates.common.FieldValidator.PERSON_NAME_ERROR_MESSAGE;
import static teammates.common.FieldValidator.REASON_EMPTY;
import static teammates.common.FieldValidator.REASON_INCORRECT_FORMAT;
import static org.testng.AssertJUnit.*;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.Common;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.InvalidParametersException;
import teammates.storage.api.AccountsDb;
import teammates.storage.api.InstructorsDb;
import teammates.storage.datastore.Datastore;
import teammates.test.cases.BaseTestCase;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class InstructorsDbTest extends BaseTestCase {
	
	//TODO: add missing test cases, refine existing ones. Follow the example
	//  of CoursesDbTest::testCreateCourse().

	private InstructorsDb instructorsDb = new InstructorsDb();
	private static LocalServiceTestHelper helper;
	
	@BeforeClass
	public static void setupClass() throws Exception {
		printTestClassHeader();
		turnLoggingUp(AccountsDb.class);
		Datastore.initialize();
		LocalDatastoreServiceTestConfig localDatastore = new LocalDatastoreServiceTestConfig();
		helper = new LocalServiceTestHelper(localDatastore);
		helper.setUp();
	}
	
	@Test
	public void testCreateInstructor() 
			throws EntityAlreadyExistsException, InvalidParametersException {
		// SUCCESS
		InstructorAttributes i = new InstructorAttributes();
		i.googleId = "valid.fresh.id";
		i.courseId = "valid.course.Id";
		i.name = "valid.name";
		i.email = "valid@email.com";
		instructorsDb.createInstructor(i);
		
		// FAIL : duplicate
		try {
			instructorsDb.createInstructor(i);
			Assert.fail();
		} catch (EntityAlreadyExistsException e) {
			assertContains(AccountsDb.ERROR_CREATE_INSTRUCTOR_ALREADY_EXISTS, e.getMessage());
		}
		
		// FAIL : invalid params
		i.googleId = "invalid id with spaces";
		try {
			instructorsDb.createInstructor(i);
			Assert.fail();
		} catch (InvalidParametersException e) {
			assertContains(
				"Invalid parameter detected while adding instructor :[\"invalid id with spaces\"",
				e.getMessage()); 
		} catch (EntityAlreadyExistsException e) {
			Assert.fail();
		}
		
		// Null params check:
		try {
			instructorsDb.createInstructor(null);
			Assert.fail();
		} catch (AssertionError a) {
			assertEquals(Common.ERROR_DBLEVEL_NULL_INPUT, a.getMessage());
		}
	}
	
	@Test
	public void testGetInstructor() throws InvalidParametersException {
		InstructorAttributes i = createNewInstructor();
		
		// Get existent
		InstructorAttributes retrieved = instructorsDb.getInstructorForGoogleId(i.courseId, i.googleId);
		assertNotNull(retrieved);
		
		// Get non-existent - just return null
		retrieved = instructorsDb.getInstructorForGoogleId("non.existent.course", "non.existent");
		assertNull(retrieved);
		
		// Null params check:
		try {
			instructorsDb.getInstructorForGoogleId(null, null);
			Assert.fail();
		} catch (AssertionError a) {
			assertEquals(Common.ERROR_DBLEVEL_NULL_INPUT, a.getMessage());
		}
	}
	
	@Test
	public void testUpdateInstructor() throws InvalidParametersException {
		InstructorAttributes instructorToEdit = createNewInstructor();
		
		// SUCCESS
		// Test for old value
		assertEquals("valid.name", instructorToEdit.name);
		assertEquals("valid@email.com", instructorToEdit.email);
		
		// instructorToEdit is already inside, we can just edit and test
		instructorToEdit.name = "My New Name";
		instructorToEdit.email = "new@email.com";
		instructorsDb.updateInstructor(instructorToEdit);
		
		// Re-retrieve
		instructorToEdit = instructorsDb.getInstructorForGoogleId(instructorToEdit.courseId, instructorToEdit.googleId);
		assertEquals("My New Name", instructorToEdit.name);
		assertEquals("new@email.com", instructorToEdit.email);
		
		// FAIL : invalid parameters
		instructorToEdit.name = "";
		instructorToEdit.email = "aaa";
		try {
			instructorsDb.updateInstructor(instructorToEdit);
			Assert.fail();
		} catch (InvalidParametersException e) {
			assertContains(
						String.format(PERSON_NAME_ERROR_MESSAGE, instructorToEdit.name,	REASON_EMPTY) + EOL 
						+ String.format(EMAIL_ERROR_MESSAGE, instructorToEdit.email,	REASON_INCORRECT_FORMAT), 
					e.getMessage());
		}
		
		// Null parameters check:
		try {
			instructorsDb.updateInstructor(null);
		} catch (AssertionError ae) {
			assertEquals(Common.ERROR_DBLEVEL_NULL_INPUT, ae.getMessage());
		}
	}
	
	@Test
	public void testDeleteInstructor() throws InvalidParametersException {
		InstructorAttributes i = createNewInstructor();
		
		// Delete
		instructorsDb.deleteInstructor(i.courseId, i.googleId);
		
		InstructorAttributes deleted = instructorsDb.getInstructorForGoogleId(i.courseId, i.googleId);
		assertNull(deleted);
		
		// delete again - should fail silently
		instructorsDb.deleteInstructor(i.courseId, i.googleId);
		
		// Null params check:
		try {
			instructorsDb.deleteInstructor(null, null);
			Assert.fail();
		} catch (AssertionError a) {
			assertEquals(Common.ERROR_DBLEVEL_NULL_INPUT, a.getMessage());
		}
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		turnLoggingDown(AccountsDb.class);
		helper.tearDown();
	}
	
	private InstructorAttributes createNewInstructor() throws InvalidParametersException {
		InstructorAttributes c = new InstructorAttributes();
		c.googleId = "valid.id";
		c.courseId = "valid.course";
		c.name = "valid.name";
		c.email = "valid@email.com";
				
		try {
			instructorsDb.createInstructor(c);
		} catch (EntityAlreadyExistsException e) {
			// Okay if it's already inside
		}
		
		return c;
	}
}