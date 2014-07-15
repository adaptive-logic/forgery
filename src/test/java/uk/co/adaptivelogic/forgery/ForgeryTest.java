package uk.co.adaptivelogic.forgery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.co.adaptivelogic.forgery.domain.Employee;
import uk.co.adaptivelogic.forgery.domain.Manager;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;

/**
 * Unit test to test and document the basic usage of {@link Forgery}
 */
public class ForgeryTest {

	@Rule
	public ExpectedException expectedException = none();

	@Test
	public void shouldCreateInstanceOfClass() {
		// When
		String string = new Forgery.Builder().build().forge(String.class);

		// Then
		assertThat(string, is(notNullValue()));
	}

	@Test
	public void shouldCreateInstanceOfClassWithProperties() {
		// When
		Employee person = new Forgery.Builder().build().forge(Employee.class);

		// Then
		assertThat(person, is(notNullValue()));
		assertThat(person.getFirstName(), is(notNullValue()));
		assertThat(person.getLastName(), is(notNullValue()));
	}

	@Test
	public void shouldUseForgerForForgingClass() throws ServiceException {
		Forger<String> forger = new Forger<String>() {
			public String forge() {
				return "Example";
			}

			@Override
			public void service(ServiceLocator locator) throws ServiceException {

			}
		};
		// When
		String string = new Forgery.Builder().withForger(forger).build().forge(String.class);

		// Then
		assertThat(string, is("Example"));
	}

	@Test
	public void shouldThrowNullPointerWhenPassingNullObjectWithUsefulMessageForUser() {
		// Then
		expectedException.expect(NullPointerException.class);
		expectedException.expectMessage("Mission Impossible attempting to forge null classes :)");

		// When
		Forgery forgery = new Forgery(new FakeForgerRegistry(), new InMemoryServiceLocator());
		forgery.forge(null);
	}

	@Test
	public void shouldCreateInstanceOfClassUsingLoadedForger() {
		// When
		Long actual = new Forgery.Builder().build().forge(Long.class);

		// Then
		assertThat(actual, is(notNullValue()));
	}

	@Test
	public void shouldFillPropertyWithRelevantData() throws ServiceException {
		// When
		Employee employee = new Forgery.Builder()
				.withForger(new FirstNameStringForger())
				.withForger(new LastNameStringForger())
				.build().forge(Employee.class);

		// Then
		assertThat(employee.getFirstName(), is("John"));
		assertThat(employee.getLastName(), is("Smith"));
	}

	@Test
	public void shouldCreateClassWithParameterizedProperty() throws ServiceException {
		// When
		Manager manager = new Forgery.Builder()
				.withForger(new FirstNameStringForger())
				.withForger(new LastNameStringForger())
				.withForger(new Forger<List<Employee>>() {
					@Override
					public List<Employee> forge() {
						return new ArrayList<Employee>();
					}

					@Override
					public void service(ServiceLocator locator)
							throws ServiceException {

					}})
				.build().forge(Manager.class);

		// Then
		assertThat(manager.getFirstName(), is("John"));
		assertThat(manager.getLastName(), is("Smith"));
	}

	@Test
	public void shouldCreateParameterizedType() throws ServiceException {
		// When
		List<Employee> employees = new Forgery.Builder()
				.withForger(new Forger<List<Employee>>() {
					@Override
					public List<Employee> forge() {
						return new ArrayList<Employee>();
					}

					@Override
					public void service(ServiceLocator locator)
							throws ServiceException {

					}})
				.build().forge(new TypeToken<List<Employee>>() {}.getType());

		// Then
		assertThat(employees, is(notNullValue()));
	}

	@Test
	public void shouldWorkMultipleTimes() {
		System.out.println("Caching Test");
		// When
		Forgery forgery = new Forgery.Builder().build();

		// Then
		assertThat(forgery.forge(Employee.class), is(notNullValue()));
		assertThat(forgery.forge(Employee.class), is(notNullValue()));
		assertThat(forgery.forge(Employee.class), is(notNullValue()));
	}

	@Test
	public void shouldFailForInaccessibleType() {
		// Then
		expectedException.expectCause(isA(IllegalAccessException.class));

		// When
		Forgery forgery = new Forgery(new FakeForgerRegistry(), new InMemoryServiceLocator());
		forgery.forge(System.class);
	}

	@Test
	public void shouldFailForUninstantiableType() {
		// Then
		expectedException.expectCause(isA(InstantiationException.class));

		// When
		Forgery forgery = new Forgery(new FakeForgerRegistry(), new InMemoryServiceLocator());
		forgery.forge(Forgery.class);
	}

	private static class FakeForgerRegistry implements ForgerRegistry {
		@Override
		public <T> void register(Forger<T> forger) {

		}

		@Override
		public <T> Optional<Forger<T>> lookup(Type type) {
			return Optional.absent();
		}

		@Override
		public <T> Optional<Forger<T>> lookup(Type type, String property) {
			return Optional.absent();
		}
	}
}
