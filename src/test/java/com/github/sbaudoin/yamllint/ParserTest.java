/**
 * Copyright (c) 2018-2023, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.yamllint;

import junit.framework.TestCase;
import org.yaml.snakeyaml.tokens.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotEquals;

public class ParserTest extends TestCase {
    public void testGetLines() {
        List<Parser.Line> e = Parser.getLines("");
        assertEquals(1, e.size());
        assertEquals(1, e.get(0).getLineNo());
        assertEquals(0, e.get(0).getStart());
        assertEquals(0, e.get(0).getEnd());

        e = Parser.getLines("\n");
        assertEquals(2, e.size());

        e = Parser.getLines(" \n");
        assertEquals(2, e.size());
        assertEquals(1, e.get(0).getLineNo());
        assertEquals(0, e.get(0).getStart());
        assertEquals(1, e.get(0).getEnd());

        e = Parser.getLines("\n\n");
        assertEquals(3, e.size());

        e = Parser.getLines("---\n" +
                "this is line 1\n" +
                "line 2\n" +
                "\n" +
                "3\n");
        assertEquals(6, e.size());
        assertEquals(1, e.get(0).getLineNo());
        assertEquals("---", e.get(0).getContent());
        assertEquals("line 2", e.get(2).getContent());
        assertEquals("", e.get(3).getContent());
        assertEquals(6, e.get(5).getLineNo());

        e = Parser.getLines("test with\n" +
                "no newline\n" +
                "at the end");
        assertEquals(3, e.size());
        assertEquals(3, e.get(2).getLineNo());
        assertEquals("at the end", e.get(2).getContent());
    }

    public void testGetTokensOrComments() {
        List<Parser.Lined> e = Parser.getTokensOrComments("");
        assertEquals(2, e.size());
        assertTrue(e.get(0) instanceof Parser.Token);
        assertEquals(null, ((Parser.Token)e.get(0)).getPrev());
        assertNotNull(((Parser.Token)e.get(0)).getCurr());
        assertNotNull(((Parser.Token)e.get(0)).getNext());
        assertTrue(e.get(1) instanceof Parser.Token);
        assertEquals(((Parser.Token)e.get(1)).getPrev(), ((Parser.Token)e.get(0)).getCurr());
        assertEquals(((Parser.Token)e.get(1)).getCurr(), ((Parser.Token)e.get(0)).getNext());
        assertEquals(null, ((Parser.Token)e.get(1)).getNext());

        e = Parser.getTokensOrComments("---\n" +
                "k: v\n");
        assertEquals(9, e.size());
        assertTrue(((Parser.Token)e.get(3)).getCurr() instanceof KeyToken);
        assertTrue(((Parser.Token)e.get(5)).getCurr() instanceof ValueToken);

        e = Parser.getTokensOrComments("# start comment\n" +
                "- a\n" +
                "- key: val  # key=val\n" +
                "# this is\n" +
                "# a block     \n" +
                "# comment\n" +
                "- c\n" +
                "# end comment\n");
        assertEquals(21, e.size());
        assertTrue(e.get(1) instanceof Parser.Comment);
        assertEquals(new Parser.Comment(1, 1, "# start comment", 0, null, null, null), e.get(1));
        assertEquals(new Parser.Comment(3, 13, "# key=val", 0, null, null, null), e.get(11));
        assertEquals(new Parser.Comment(4, 1, "# this is", 0, null, null, null), e.get(12));
        assertEquals(new Parser.Comment(5, 1, "# a block     ", 0, null, null, null), e.get(13));
        assertEquals(new Parser.Comment(6, 1, "# comment", 0, null, null, null), e.get(14));
        assertEquals(new Parser.Comment(8, 1, "# end comment", 0, null, null, null), e.get(18));

        e = Parser.getTokensOrComments("---\n" +
                "# no newline char");
        assertEquals(new Parser.Comment(2, 1, "# no newline char", 0, null, null, null), e.get(2));

        e = Parser.getTokensOrComments("# just comment");
        assertEquals(new Parser.Comment(1, 1, "# just comment", 0, null, null, null), e.get(1));

        e = Parser.getTokensOrComments("\n" +
                "   # indented comment\n");
        assertEquals(new Parser.Comment(2, 4, "# indented comment", 0, null, null, null), e.get(1));

        e = Parser.getTokensOrComments("\n" +
                "# trailing spaces    \n");
        assertEquals(new Parser.Comment(2, 1, "# trailing spaces    ", 0, null, null, null), e.get(1));

        e = Parser.getTokensOrComments("# block\n" +
                "# comment\n" +
                "- data   # inline comment\n" +
                "# block\n" +
                "# comment\n" +
                "- k: v   # inline comment\n" +
                "- [ l, ist\n" +
                "]   # inline comment\n" +
                "- { m: ap\n" +
                "}   # inline comment\n" +
                "# block comment\n" +
                "- data   # inline comment\n").stream().filter(c -> c instanceof Parser.Comment).collect(Collectors.toList());
        assertEquals(10, e.size());
        assertFalse(((Parser.Comment)e.get(0)).isInline());
        assertFalse(((Parser.Comment)e.get(1)).isInline());
        assertTrue(((Parser.Comment)e.get(2)).isInline());
        assertFalse(((Parser.Comment)e.get(3)).isInline());
        assertFalse(((Parser.Comment)e.get(4)).isInline());
        assertTrue(((Parser.Comment)e.get(5)).isInline());
        assertTrue(((Parser.Comment)e.get(6)).isInline());
        assertTrue(((Parser.Comment)e.get(7)).isInline());
        assertFalse(((Parser.Comment)e.get(8)).isInline());
        assertTrue(((Parser.Comment)e.get(9)).isInline());
    }

    public void testGetTokensOrCommentsOrLines() {
        List<Parser.Lined> e = Parser.getTokensOrCommentsOrLines("---\n" +
                "k: v  # k=v\n");
        assertEquals(13, e.size());
        assertTrue(e.get(0) instanceof Parser.Token);
        assertTrue(((Parser.Token)e.get(0)).getCurr() instanceof StreamStartToken);
        assertTrue(e.get(1) instanceof Parser.Token);
        assertTrue(((Parser.Token)e.get(1)).getCurr() instanceof DocumentStartToken);
        assertTrue(e.get(2) instanceof Parser.Line);
        assertTrue(e.get(3) instanceof Parser.Token);
        assertTrue(((Parser.Token)e.get(3)).getCurr() instanceof BlockMappingStartToken);
        assertTrue(e.get(4) instanceof Parser.Token);
        assertTrue(((Parser.Token)e.get(4)).getCurr() instanceof KeyToken);
        assertTrue(e.get(6) instanceof Parser.Token);
        assertTrue(((Parser.Token)e.get(6)).getCurr() instanceof ValueToken);
        assertTrue(e.get(8) instanceof Parser.Comment);
        assertTrue(e.get(9) instanceof Parser.Line);
        assertTrue(e.get(12) instanceof Parser.Line);
    }

    public void testCommentEquals() {
        String buffer = "---\n" +
                "k: v  # k=v\n";
        List<Parser.Lined> e = Parser.getTokensOrCommentsOrLines(buffer);
        assertTrue(e.get(8) instanceof Parser.Comment);
        assertNotEquals(e.get(8), e.get(4));
        assertEquals(new Parser.Comment(2, 7, buffer, 10, null, null, null), e.get(8));
    }
}
