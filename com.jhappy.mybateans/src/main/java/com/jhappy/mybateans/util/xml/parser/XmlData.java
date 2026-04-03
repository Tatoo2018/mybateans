/*
 * The MIT License
 *
 * Copyright 2026 th.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.jhappy.mybateans.util.xml.parser;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.openide.filesystems.FileObject;
import org.netbeans.api.lexer.Token;

public class XmlData {

    /**
     * @return the tagName
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * @return the attributes
     */
    public Map<String, AttributeData> getAttributes() {
        return attributes;
    }

    /**
     * @return the children
     */
    public List<XmlData> getChildren() {
        return children;
    }

    private final String tagName;
    private final Map<String, AttributeData> attributes = new HashMap<>();
    private final List<XmlData> children = new ArrayList<>();

    XmlData(String tagName) {
        this.tagName = tagName;
    }

    /**
     */
    public List<XmlData> select(String... path) {

        return findRecursive(this, path, 0);
    }

    private List<XmlData> findRecursive(XmlData current, String[] parts, int index) {
        if (index >= parts.length) {
            return Collections.emptyList();
        }

        String target = parts[index];
        List<XmlData> matches = current.getChildren().stream()
                .filter(t -> t.getTagName().equals(target))
                .collect(Collectors.toList());

        if (index == parts.length - 1) {
            return matches;
        } else {
            List<XmlData> result = new ArrayList<>();
            for (XmlData m : matches) {
                result.addAll(findRecursive(m, parts, index + 1));
            }
            return result;
        }
    }

    public static XmlData parseFullXml(FileObject fo) {
     
        
        try {
        
            String text = fo.asText();
            TokenHierarchy<String> th = TokenHierarchy.create(text, XMLTokenId.language());
            TokenSequence<XMLTokenId> ts = th.tokenSequence(XMLTokenId.language());

            if (ts == null) {
                return null;
            }

            XmlData root = new XmlData("ROOT");
            Deque<XmlData> stack = new ArrayDeque<>();
            stack.push(root);

            while (ts.moveNext()) {

                Token<XMLTokenId> token = ts.token();

                XMLTokenId id = token.id();

                if (id == XMLTokenId.TAG) {
                    
                    String tagText = token.text().toString();

                    
                    if (tagText.startsWith("</")) {
                        
                        if (stack.size() > 1) {
                            stack.pop();
                        }
                        
                    } else if (tagText.startsWith("<")) {
                        
                        String tagName = tagText.substring(1).trim();
                        
                        XmlData newTag = new XmlData(tagName);

                        stack.peek().getChildren().add(newTag);

                        //tagの開始位置を一旦記録
                        int lookaheadOffset = ts.offset();

                        while (ts.moveNext()) {

                            Token<XMLTokenId> nextTok = ts.token();

                            if (nextTok.id() == XMLTokenId.TAG) {

                                String nextText = nextTok.text().toString();

                                if (nextText.contains("/>")) {
                                    break;
                                }
                                if (nextText.contains(">")) {
                                    break;
                                }
                            }
                        }

                        stack.push(newTag);

                        ts.move(lookaheadOffset);
                        
                        ts.moveNext();
                        
                    } else if (tagText.contains(">")) {
                        
                        if (tagText.contains("/>")) {
                            if (stack.size() > 1) {
                                stack.pop();
                            }
                        }
                    }
                }

                if (id == XMLTokenId.ARGUMENT) {
                    
                    String attrName = token.text().toString();
                    
                    int nameOff = ts.offset();

                    while (ts.moveNext() && ts.token().id() != XMLTokenId.VALUE) {
                    }

                    Token<XMLTokenId> valToken = ts.token();
                    
                    if (valToken != null && valToken.id() == XMLTokenId.VALUE) {
                     
                        String fullVal = valToken.text().toString();
                        
                        if (fullVal.length() >= 2) {
                            String pureValue = fullVal.substring(1, fullVal.length() - 1);
                            
                            stack.peek().getAttributes().put(attrName,
                                    new AttributeData(attrName, nameOff, pureValue, ts.offset() + 1));
                        }
                    }
                }
            }
            return root;

        } catch (IOException ex) {
            return null;
        }
    }
}
