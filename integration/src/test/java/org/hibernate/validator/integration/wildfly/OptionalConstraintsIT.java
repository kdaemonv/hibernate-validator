/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Future;

import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.integration.AbstractArquillianIT;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

/**
 * Asserts that the constraints based on the JodaTime and JSoup server modules can be used.
 *
 * @author Gunnar Morling
 */
public class OptionalConstraintsIT extends AbstractArquillianIT {

	private static final String WAR_FILE_NAME = OptionalConstraintsIT.class
			.getSimpleName() + ".war";

	public static class Shipment {

		@Future
		public DateTime deliveryDate = new DateTime( 2014, 10, 21, 0, 0, 0, 0 );
	}

	public static class Item {

		@SafeHtml
		public String descriptionHtml = "<script>Cross-site scripting https://en.wikipedia.org/wiki/42<script/>";
	}

	@Deployment
	public static Archive<?> createTestArchive() {
		return buildTestArchive( WAR_FILE_NAME )
				.addAsWebInfResource( "jboss-deployment-structure-optional-constraints.xml", "jboss-deployment-structure.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	private Validator validator;

	@Test
	public void canUseJodaTimeConstraintValidator() {
		Set<ConstraintViolation<Shipment>> violations = validator.validate( new Shipment() );


		assertThat( violations.size() ).isEqualTo( 1 );
		assertThat( violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType() ).isEqualTo( Future.class );
	}

	@Test
	public void canUseJsoupBasedConstraint() {
		Set<ConstraintViolation<Item>> violations = validator.validate( new Item() );

		assertThat( violations.size() ).isEqualTo( 1 );
		assertThat( violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType() ).isEqualTo( SafeHtml.class );
	}
}
