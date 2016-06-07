package org.powerassert;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

public interface PowerAssertGenerator {
	void init(ProcessingEnvironment env);

	/**
	 * @return true if the generator successfully generated power asserts, false otherwise
	 * @param element - the element to scan
	 */
	boolean scan(Element element);
}
