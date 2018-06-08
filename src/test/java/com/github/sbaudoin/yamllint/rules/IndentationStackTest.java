/**
 * Copyright (c) 2018, Sylvain Baudoin
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
package com.github.sbaudoin.yamllint.rules;

import junit.framework.TestCase;
import com.github.sbaudoin.yamllint.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IndentationStackTest extends TestCase {
    /**
     * Transform the stack at a given moment into a printable string like:
     * <pre>B_MAP:0 KEY:0 VAL:5</pre>
     */
    public String formatStack(List stack) {
        return (String)stack.stream().filter(e -> stack.indexOf(e) > 0).map(Object::toString).collect(Collectors.joining(" "));
    }

    public String fullStack(String source) {
        Map<String, Object> conf = new HashMap<String, Object>() {
            {
                put("spaces", 2);
                put("indent-sequences", true);
                put("check-multi-line-strings", false);
            }
        };

        Map<String, Object> context = new HashMap<>();
        String output = "";
        for (Parser.Lined elem : Parser.getTokensOrComments(source).stream().filter(t -> !(t instanceof Parser.Comment)).collect(Collectors.toList())) {
            // Get the context
            new Indentation().check(conf, ((Parser.Token)elem).getCurr(), ((Parser.Token)elem).getPrev(), ((Parser.Token)elem).getNext(), ((Parser.Token)elem).getNextNext(), context);

            String tokenType = ((Parser.Token)elem).getCurr().getClass().getSimpleName()
                    .replaceAll("Token", "")
                    .replaceAll("Block", "B").replaceAll("Flow", "F")
                    .replaceAll("Sequence", "Seq")
                    .replaceAll("Mapping", "Map");
            if ("StreamStart".equals(tokenType) || "StreamEnd".equals(tokenType)) {
                continue;
            }
            output += String.format("%9s %s\n", tokenType, formatStack((List)context.get("stack")));
        }

        return output;
    }

    public void testSimpleMapping() {
        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:5\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("key: val\n"));

        assertEquals(
                "BMapStart B_MAP:5\n" +
                "      Key B_MAP:5 KEY:5\n" +
                "   Scalar B_MAP:5 KEY:5\n" +
                "    Value B_MAP:5 KEY:5 VAL:10\n" +
                "   Scalar B_MAP:5\n" +
                "     BEnd \n",
                fullStack("     key: val\n"));
    }

    public void testSimpleSequence() {
        assertEquals(
                "BSeqStart B_SEQ:0\n" +
                "   BEntry B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_SEQ:0\n" +
                "   BEntry B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_SEQ:0\n" +
                "   BEntry B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_SEQ:0\n" +
                "     BEnd \n",
                fullStack("- 1\n" +
                        "- 2\n" +
                        "- 3\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                "BSeqStart B_MAP:0 KEY:0 VAL:2 B_SEQ:2\n" +
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:2 B_ENT:4\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:2\n" +
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:2 B_ENT:4\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:2\n" +
                "     BEnd B_MAP:0\n" +
                "     BEnd \n",
                fullStack("key:\n" +
                        "  - 1\n" +
                        "  - 2\n"));
    }

    public void testNonIndentedSequences() {
            /* There seems to be a bug in snakeyaml: depending on the indentation, a
               sequence does not produce the same tokens. More precisely, the
               following YAML:
                   usr:
                     - lib
               produces a BlockSequenceStartToken and a BlockEndToken around the
               "lib" sequence, whereas the following:
                   usr:
                     - lib
               does not (both two tokens are omitted).
               So, yamllint must create fake 'B_SEQ'. This test makes sure it does. */

                assertEquals(
                        "BMapStart B_MAP:0\n" +
                        "      Key B_MAP:0 KEY:0\n" +
                        "   Scalar B_MAP:0 KEY:0\n" +
                        "    Value B_MAP:0 KEY:0 VAL:2\n" +
                        "BSeqStart B_MAP:0 KEY:0 VAL:2 B_SEQ:2\n" +
                        "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:2 B_ENT:4\n" +
                        "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:2\n" +
                        "     BEnd B_MAP:0\n" +
                        "      Key B_MAP:0 KEY:0\n" +
                        "   Scalar B_MAP:0 KEY:0\n" +
                        "    Value B_MAP:0 KEY:0 VAL:5\n" +
                        "   Scalar B_MAP:0\n" +
                        "     BEnd \n",
                        fullStack("usr:\n" +
                                "  - lib\n" +
                                "var: cache\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_MAP:0\n" +
                // missing BEnd here
                "     BEnd \n",
                fullStack("usr:\n" +
                        "- lib\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_MAP:0\n" +
                // missing BEnd here
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:5\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("usr:\n" +
                        "- lib\n" +
                        "var: cache\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "FSeqStart B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 F_SEQ:3\n" +
                "  FSeqEnd B_MAP:0\n" +
                // missing BEnd here
                "     BEnd \n",
                fullStack("usr:\n" +
                        "- []\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "BMapStart B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_MAP:2\n" +
                "      Key B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2\n" +
                "    Value B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2 VAL:4\n" +  // noqa
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_MAP:2\n" +
                "     BEnd B_MAP:0\n" +
                // missing BEnd here
                "     BEnd \n",
                fullStack("usr:\n" +
                        "- k:\n" +
                        "    v\n"));
    }

    public void testFlows() {
        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:5\n" +
                "FSeqStart B_MAP:0 KEY:0 VAL:5 F_SEQ:2\n" +
                "FMapStart B_MAP:0 KEY:0 VAL:5 F_SEQ:2 F_MAP:3\n" +
                "      Key B_MAP:0 KEY:0 VAL:5 F_SEQ:2 F_MAP:3 KEY:3\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:5 F_SEQ:2 F_MAP:3 KEY:3\n" +
                "    Value B_MAP:0 KEY:0 VAL:5 F_SEQ:2 F_MAP:3 KEY:3 VAL:5\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:5 F_SEQ:2 F_MAP:3\n" +
                "  FMapEnd B_MAP:0 KEY:0 VAL:5 F_SEQ:2\n" +
                "  FSeqEnd B_MAP:0\n" +
                "     BEnd \n",
                fullStack("usr: [\n" +
                        "  {k:\n" +
                        "    v}\n" +
                        "  ]\n"));
    }

    public void testAnchors() {
        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:5\n" +
                "   Anchor B_MAP:0 KEY:0 VAL:5\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("key: &anchor value\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                "   Anchor B_MAP:0 KEY:0 VAL:2\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("key: &anchor\n" +
                        "  value\n"));

        assertEquals(
                "BSeqStart B_SEQ:0\n" +
                "   BEntry B_SEQ:0 B_ENT:2\n" +
                "   Anchor B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_SEQ:0\n" +
                "     BEnd \n",
                fullStack("- &anchor value\n"));

        assertEquals(
                "BSeqStart B_SEQ:0\n" +
                "   BEntry B_SEQ:0 B_ENT:2\n" +
                "   Anchor B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_SEQ:0\n" +
                "     BEnd \n",
                fullStack("- &anchor\n" +
                        "  value\n"));

        assertEquals(
                "BSeqStart B_SEQ:0\n" +
                "   BEntry B_SEQ:0 B_ENT:2\n" +
                "   Anchor B_SEQ:0 B_ENT:2\n" +
                "BSeqStart B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "   BEntry B_SEQ:0 B_ENT:2 B_SEQ:2 B_ENT:4\n" +
                "   Scalar B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "   BEntry B_SEQ:0 B_ENT:2 B_SEQ:2 B_ENT:4\n" +
                "   Scalar B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "     BEnd B_SEQ:0\n" +
                "     BEnd \n",
                fullStack("- &anchor\n" +
                        "  - 1\n" +
                        "  - 2\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Anchor B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("&anchor key:\n" +
                        "  value\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                "   Anchor B_MAP:0 KEY:0 VAL:2\n" +
                "   Scalar B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Anchor B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("pre:\n" +
                        "  &anchor1 0\n" +
                        "&anchor2 key:\n" +
                        "  value\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                "   Anchor B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0\n" +
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "   Anchor B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "BSeqStart B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2 B_ENT:4\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "     BEnd B_MAP:0\n" +
                // missing BEnd here
                "     BEnd \n",
                fullStack("sequence: &anchor\n" +
                        "- entry\n" +
                        "- &anchor\n" +
                        "  - nested\n"));
    }

    public void testTags() {
        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:5\n" +
                "      Tag B_MAP:0 KEY:0 VAL:5\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("key: !!tag value\n"));

        assertEquals(
                "BSeqStart B_SEQ:0\n" +
                "   BEntry B_SEQ:0 B_ENT:2\n" +
                "      Tag B_SEQ:0 B_ENT:2\n" +
                "BMapStart B_SEQ:0 B_ENT:2 B_MAP:2\n" +
                "      Key B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2\n" +
                "   Scalar B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2\n" +
                "    Value B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2 VAL:8\n" +
                "   Scalar B_SEQ:0 B_ENT:2 B_MAP:2\n" +
                "     BEnd B_SEQ:0\n" +
                "     BEnd \n",
                fullStack("- !!map # Block collection\n" +
                        "  foo : bar\n"));

        assertEquals(
                "BSeqStart B_SEQ:0\n" +
                "   BEntry B_SEQ:0 B_ENT:2\n" +
                "      Tag B_SEQ:0 B_ENT:2\n" +
                "BSeqStart B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "   BEntry B_SEQ:0 B_ENT:2 B_SEQ:2 B_ENT:4\n" +
                "   Scalar B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "     BEnd B_SEQ:0\n" +
                "     BEnd \n",
                fullStack("- !!seq\n" +
                        "  - nested item\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                "      Tag B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0\n" +
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "      Tag B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "BSeqStart B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2 B_ENT:4\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "     BEnd B_MAP:0\n" +
                // missing BEnd here
                "     BEnd \n",
                fullStack("sequence: !!seq\n" +
                        "- entry\n" +
                        "- !!seq\n" +
                        "  - nested\n"));
    }

    public void testFlowsImbrication() {
        assertEquals(
                "FSeqStart F_SEQ:1\n" +
                "FSeqStart F_SEQ:1 F_SEQ:2\n" +
                "   Scalar F_SEQ:1 F_SEQ:2\n" +
                "  FSeqEnd F_SEQ:1\n" +
                "  FSeqEnd \n",
                fullStack("[[val]]\n"));

        assertEquals(
                "FSeqStart F_SEQ:1\n" +
                "FSeqStart F_SEQ:1 F_SEQ:2\n" +
                "   Scalar F_SEQ:1 F_SEQ:2\n" +
                "  FSeqEnd F_SEQ:1\n" +
                "   FEntry F_SEQ:1\n" +
                "FSeqStart F_SEQ:1 F_SEQ:9\n" +
                "   Scalar F_SEQ:1 F_SEQ:9\n" +
                "  FSeqEnd F_SEQ:1\n" +
                "  FSeqEnd \n",
                fullStack("[[val], [val2]]\n"));

        assertEquals(
                "FMapStart F_MAP:1\n" +
                "FMapStart F_MAP:1 F_MAP:2\n" +
                "   Scalar F_MAP:1 F_MAP:2\n" +
                "  FMapEnd F_MAP:1\n" +
                "  FMapEnd \n",
                fullStack("{{key}}\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "FSeqStart B_MAP:0 KEY:0 F_SEQ:1\n" +
                "   Scalar B_MAP:0 KEY:0 F_SEQ:1\n" +
                "  FSeqEnd B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:7\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("[key]: value\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "FSeqStart B_MAP:0 KEY:0 F_SEQ:1\n" +
                "FSeqStart B_MAP:0 KEY:0 F_SEQ:1 F_SEQ:2\n" +
                "   Scalar B_MAP:0 KEY:0 F_SEQ:1 F_SEQ:2\n" +
                "  FSeqEnd B_MAP:0 KEY:0 F_SEQ:1\n" +
                "  FSeqEnd B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:9\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("[[key]]: value\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "FMapStart B_MAP:0 KEY:0 F_MAP:1\n" +
                "   Scalar B_MAP:0 KEY:0 F_MAP:1\n" +
                "  FMapEnd B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:7\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("{key}: value\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "FMapStart B_MAP:0 KEY:0 F_MAP:1\n" +
                "      Key B_MAP:0 KEY:0 F_MAP:1 KEY:1\n" +
                "   Scalar B_MAP:0 KEY:0 F_MAP:1 KEY:1\n" +
                "    Value B_MAP:0 KEY:0 F_MAP:1 KEY:1 VAL:6\n" +
                "   Scalar B_MAP:0 KEY:0 F_MAP:1\n" +
                "  FMapEnd B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:14\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("{key: value}: value\n"));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "FMapStart B_MAP:0 KEY:0 F_MAP:1\n" +
                "FMapStart B_MAP:0 KEY:0 F_MAP:1 F_MAP:2\n" +
                "   Scalar B_MAP:0 KEY:0 F_MAP:1 F_MAP:2\n" +
                "  FMapEnd B_MAP:0 KEY:0 F_MAP:1\n" +
                "  FMapEnd B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:9\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("{{key}}: value\n"));
        assertEquals(
                "FMapStart F_MAP:1\n" +
                "      Key F_MAP:1 KEY:1\n" +
                "FMapStart F_MAP:1 KEY:1 F_MAP:2\n" +
                "   Scalar F_MAP:1 KEY:1 F_MAP:2\n" +
                "  FMapEnd F_MAP:1 KEY:1\n" +
                "    Value F_MAP:1 KEY:1 VAL:8\n" +
                "   Scalar F_MAP:1\n" +
                "   FEntry F_MAP:1\n" +
                "      Key F_MAP:1 KEY:1\n" +
                "FMapStart F_MAP:1 KEY:1 F_MAP:14\n" +
                "   Scalar F_MAP:1 KEY:1 F_MAP:14\n" +
                "  FMapEnd F_MAP:1 KEY:1\n" +
                "    Value F_MAP:1 KEY:1 VAL:21\n" +
                "FMapStart F_MAP:1 KEY:1 VAL:21 F_MAP:22\n" +
                "   Scalar F_MAP:1 KEY:1 VAL:21 F_MAP:22\n" +
                "  FMapEnd F_MAP:1\n" +
                "  FMapEnd \n",
                fullStack("{{key}: val, {key2}: {val2}}\n"));

        assertEquals(
                "FMapStart F_MAP:1\n" +
                "FSeqStart F_MAP:1 F_SEQ:2\n" +
                "FMapStart F_MAP:1 F_SEQ:2 F_MAP:3\n" +
                "FMapStart F_MAP:1 F_SEQ:2 F_MAP:3 F_MAP:4\n" +
                "FSeqStart F_MAP:1 F_SEQ:2 F_MAP:3 F_MAP:4 F_SEQ:5\n" +
                "   Scalar F_MAP:1 F_SEQ:2 F_MAP:3 F_MAP:4 F_SEQ:5\n" +
                "  FSeqEnd F_MAP:1 F_SEQ:2 F_MAP:3 F_MAP:4\n" +
                "  FMapEnd F_MAP:1 F_SEQ:2 F_MAP:3\n" +
                "  FMapEnd F_MAP:1 F_SEQ:2\n" +
                "   FEntry F_MAP:1 F_SEQ:2\n" +
                "FSeqStart F_MAP:1 F_SEQ:2 F_SEQ:14\n" +
                "FMapStart F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15\n" +
                "      Key F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15 KEY:15\n" +
                "FSeqStart F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15 KEY:15 F_SEQ:16\n" +
                "   Scalar F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15 KEY:15 F_SEQ:16\n" +
                "  FSeqEnd F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15 KEY:15\n" +
                "    Value F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15 KEY:15 VAL:22\n" +
                "   Scalar F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15\n" +
                "  FMapEnd F_MAP:1 F_SEQ:2 F_SEQ:14\n" +
                "  FSeqEnd F_MAP:1 F_SEQ:2\n" +
                "  FSeqEnd F_MAP:1\n" +
                "  FMapEnd \n",
                fullStack("{[{{[val]}}, [{[key]: val2}]]}\n"));
    }
}