package io.github.nibiruos.retrosoap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.QName;

class ParserAdapter {
    private final XmlPullParser parser;

    ParserAdapter(XmlPullParser parser) {
        this.parser = parser;
    }

    void next() {
        try {
            parser.next();
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    int getEventType() {
        try {
            return parser.getEventType();
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    String getName() {
        return parser.getName();
    }

    String getPrefix() {
        return parser.getPrefix();
    }

    String getNamespace() {
        return parser.getNamespace();
    }

    String getText() {
        return parser.getText();
    }

    String getNamespace(String prefix) {
        return parser.getNamespace(prefix);
    }

    boolean isTagStart(QName tag) {
        return isTagStart() && isName(tag);
    }

    boolean isTagEnd(QName tag) {
        return isTagEnd() && isName(tag);
    }

    boolean isDocumentEnd() {
        return getEventType() == XmlPullParser.END_DOCUMENT;
    }

    String getAttribute(String name) {
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (name.equals(parser.getAttributeName(i))) {
                return parser.getAttributeValue(i);
            }
        }
        return null;
    }

    Iterable<Attribute> getAttributes() {
        return new Iterable<Attribute>() {
            @Override
            public Iterator<Attribute> iterator() {
                return new AttributeIterator();
            }
        };
    }

    private boolean isName(QName name) {
        return isName(name.getLocalPart())
                && isNamespace(name.getNamespaceURI());
    }

    private boolean isName(String name) {
        return name.equals(parser.getName());
    }

    private boolean isNamespace(String namespace) {
        return namespace.equals(parser.getNamespace());
    }

    boolean isTagStart() {
        return getEventType() == XmlPullParser.START_TAG;
    }

    private boolean isTagEnd() {
        return getEventType() == XmlPullParser.END_TAG;
    }


    interface Attribute {
        String getName();

        String getValue();

        String getPrefix();

        String getNamespace();
    }

    private class AttributeIterator
            implements Iterator<Attribute> {
        private int next;


        @Override
        public boolean hasNext() {
            return next < parser.getAttributeCount();
        }

        @Override
        public Attribute next() {
            Attribute attribute = new Attribute() {
                int current = next;

                @Override
                public String getName() {
                    return parser.getAttributeName(current);
                }

                @Override
                public String getValue() {
                    return parser.getAttributeValue(current);
                }

                @Override
                public String getPrefix() {
                    return parser.getAttributePrefix(current);
                }

                @Override
                public String getNamespace() {
                    return parser.getAttributeNamespace(current);
                }
            };
            next++;
            return attribute;
        }
    }
}
