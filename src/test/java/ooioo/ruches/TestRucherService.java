package ooioo.ruches;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ooioo.ruches.rucher.RucherService;

@SpringBootTest
public class TestRucherService {

	@Autowired
	private RucherService rucherService;

	@Test
	public void testDistance() {
		double distAttendue = 14.81;
		double dist = rucherService.distance(45.46271, 45.46262, 3.82712, 3.82698);
		assertEquals(distAttendue, dist, 0.01);
	}
}
