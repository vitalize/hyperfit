package org.hyperfit.resource.html5.controls.form;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hyperfit.resource.controls.form.TelephoneNumberField;
import org.jsoup.nodes.Element;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JsoupHtml5TelephoneNumberField extends JsoupHtml5Field implements TelephoneNumberField {

    private final String value;

    public JsoupHtml5TelephoneNumberField(Element inputElement, Element formElement){
        super(inputElement, formElement);

        value = inputElement.attr("value");
    }

    @Override
    public String getValue() {
        return value;
    }
}
