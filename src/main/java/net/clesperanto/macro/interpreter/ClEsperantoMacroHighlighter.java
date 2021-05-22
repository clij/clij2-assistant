/*
 * #%L
 * Script Editor and Interpreter for SciJava script languages.
 * %%
 * Copyright (C) 2009 - 2018 Board of Regents of the University of
 * Wisconsin-Madison, Max Planck Institute of Molecular Cell Biology and
 * Genetics, and others.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.clesperanto.macro.interpreter;

import ij.macro.Interpreter;
import net.clesperanto.macro.api.ClEsperantoMacroAPI;
import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.script.SyntaxHighlighter;
import org.scijava.ui.swing.script.highliters.ImageJMacroTokenMaker;

/**
 * SyntaxHighliter for ij1-macros.
 *
 * @author Robert Haase
 */
@Plugin(type = SyntaxHighlighter.class, name = "clesperanto-macro")
public class ClEsperantoMacroHighlighter extends ImageJMacroTokenMaker implements
	SyntaxHighlighter
{
	public ClEsperantoMacroHighlighter() {
		super();

		String additionalFunctions = Interpreter.getAdditionalFunctions();

		String cle_code = ClEsperantoMacroAPI.generate();
		if (additionalFunctions == null) {
			Interpreter.setAdditionalFunctions(cle_code);
		} else if (!additionalFunctions.contains(cle_code)) {
			Interpreter.setAdditionalFunctions(additionalFunctions + cle_code);
		}
	}

	// Everything implemented in ImageJMacroTokenMaker
}
