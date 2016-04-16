package com.github.abrarsyed.jastyle;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

final class IndentContext
{
    private final boolean useProperInnerClassIndenting;
    private boolean reentryPermitted;
    private Stack<String> headerStack; // This is not a copy.
    private String line;
    private int previousIndent;
    private int indent;
    private final Map<Integer, String> stack = new HashMap<Integer, String>();

    IndentContext(boolean useProperInnerClassIndenting)
    {
        this.useProperInnerClassIndenting = useProperInnerClassIndenting;
    }

    void setStack(Stack<String> stack)
    {
        this.headerStack = stack;

        // Copy to a map to avoid IOOBEs
        this.stack.clear();
        for (int i = 0; i < this.headerStack.size(); i++)
        {
            this.stack.put(i, this.headerStack.get(i));
        }
    }

    void setLine(String line)
    {
        this.reentryPermitted = true;
        this.line = line;
        this.previousIndent = this.indent;
    }

    int getIndent()
    {
        return this.indent;
    }

    boolean tryIndent(final int index, final int tabCount)
    {
        if (!this.reentryPermitted)
        {
            return false;
        }

        this.indent = tabCount;

        if (!this.useProperInnerClassIndenting)
        {
            this.indent++;
        }
        else
        {
            if (this.headerStack.contains(ASResource.AS_CLASS) && this.headerStack.contains(ASResource.AS_OPEN_BRACKET))
            {
                if (!this.headerStack.get(index).equals(ASResource.AS_STATIC) || (this.headerStack.size() >= index + 2 && this.headerStack.get(index + 1).equals(ASResource.AS_OPEN_BRACKET)))
                {
                    this.indent++;
                }
                else if (this.line.equals(ASResource.AS_OPEN_BRACKET) && ASResource.AS_STATIC.equals(this.stack.get(this.stack.size() - 1)) && ASResource.AS_OPEN_BRACKET.equals(this.stack.get(this.stack.size() - 2)))
                {
                    this.indent++;
                    if (ASResource.AS_CLASS.equals(this.stack.get(this.stack.size() - 3)) && ASResource.AS_STATIC.equals(this.stack.get(this.stack.size() - 4)))
                    {
                        this.indent++;
                        this.reentryPermitted = false;
                        if (this.stack.size() > 3 && this.indent <= this.previousIndent)
                        {
                            this.indent = this.previousIndent + 1;
                        }
                    }
                }
            }
            else
            {
                this.indent++;
            }
        }

        return true;
    }
}
