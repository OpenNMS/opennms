/*
 * @(#)MacEditorKit.java  1.0  December 1, 2004
 *
 * Copyright (c) 2004 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 *
 * Part of this software (as marked) has been derived from software by
 * Dustin Sacks. These parts are used under license.
 */
package org.opennms.poller.remote;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import java.util.HashMap;

import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;

/**
 * The MacEditorKit extends the Swing DefaultEditorKit with Mac OS X specific
 * text editing actions.
 *
 * @author  Werner Randelshofer
 * @version 1.0 December 1, 2004 Created.
 */
public class MacEditorKit extends DefaultEditorKit {
    private static final long serialVersionUID = 7463251678121983829L;

    /**
     * Name of the action to delete the word that precedes the current caret
     * position.
     *
     * @see #getActions
     */
    public static final String deletePrevWordAction = "delete-previous-word";

    /**
     * Name of the action to delete the word that follows the current caret
     * position.
     *
     * @see #getActions
     */
    public static final String deleteNextWordAction = "delete-next-word";

    /** Default actions of the MacEditorKit. */
    private static final Action[] actions;

    static {
        Action[] dekActions   = new DefaultEditorKit().getActions();
        HashMap  dekActionMap = new HashMap();

        for (int i = 0; i < dekActions.length; i++) {
            dekActionMap.put(dekActions[i].getValue(Action.NAME), dekActions[i]);
        }

        HashMap actionMap = (HashMap) dekActionMap.clone();

        actionMap.put(deleteNextWordAction, new MacEditorKit.DeleteNextWordAction());
        actionMap.put(deletePrevWordAction, new MacEditorKit.DeletePrevWordAction());
        actionMap.put(upAction,
                      new MacEditorKit.VerticalAction(upAction, (TextAction) dekActionMap.get(upAction),
                                                      (TextAction) dekActionMap.get(beginAction)));
        actionMap.put(downAction,
                      new MacEditorKit.VerticalAction(downAction, (TextAction) dekActionMap.get(downAction),
                                                      (TextAction) dekActionMap.get(endAction)));
        actionMap.put(selectionUpAction,
                      new MacEditorKit.VerticalAction(selectionUpAction, (TextAction) dekActionMap.get(selectionUpAction),
                                                      (TextAction) dekActionMap.get(selectionBeginAction)));
        actionMap.put(selectionDownAction,
                      new MacEditorKit.VerticalAction(selectionDownAction, (TextAction) dekActionMap.get(selectionDownAction),
                                                      (TextAction) dekActionMap.get(selectionEndAction)));

        actions = (Action[]) actionMap.values().toArray(new Action[0]);

        // TO DO: Use this instead of the code above:
        // actions = TextAction.augmentList(....)
    }

    /**
     * Default constructor.
     */
    public MacEditorKit() {
    }

    /**
     * Fetches the set of commands that can be used on a text component that is
     * using a model and view produced by this kit.
     *
     * @return the command list
     */
    public Action[] getActions() {
        return actions;
    }

    /**
     * Invoked when the user attempts an invalid operation, such as pasting into
     * an uneditable <code>JTextField</code> that has focus. The default
     * implementation beeps. Subclasses that wish different behavior should
     * override this and provide the additional feedback.
     *
     * @param component Component the error occured in, may be null indicating
     *                  the error condition is not directly associated with a
     *                  <code>Component</code>.
     */
    static void provideErrorFeedback(Component component) {
        Toolkit toolkit = null;

        if (component != null) {
            toolkit = component.getToolkit();
        } else {
            toolkit = Toolkit.getDefaultToolkit();
        }

        toolkit.beep();
    } // provideErrorFeedback()

    /**
     * Deletes the word that follows the current caret position. Original code
     * of this class by Dustin Sacks.
     *
     * @see MacEditorKit#deleteNextWordAction
     * @see MacEditorKit#getActions
     */
    static class DeleteNextWordAction extends TextAction {
        private static final long serialVersionUID = 5038003137392979803L;

        /**
         * Creates this object with the appropriate identifier.
         */
        DeleteNextWordAction() {
            super(deleteNextWordAction);
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            boolean        beep   = true;

            if ((target != null) && (target.isEditable())) {

                try {

                    // select the next word
                    int    offs    = target.getCaretPosition();
                    int    endOffs;
                    String s       = target.getDocument().getText(offs, 1);

                    if (Character.isWhitespace(s.charAt(0))) {
                        endOffs = Utilities.getNextWord(target, offs);
                        endOffs = Utilities.getWordEnd(target, endOffs);
                    } else {
                        endOffs = Utilities.getWordEnd(target, offs);
                    }

                    target.moveCaretPosition(endOffs);

                    // and then delete it
                    target.replaceSelection("");
                    beep = false;
                } catch (BadLocationException exc) {
                    // nothing to do, because we set beep to true already
                }
            }

            if (beep) {
                provideErrorFeedback(target);
            }
        }
    }

    /**
     * Deletes the word that precedes the current caret position. Original code
     * of this class by Dustin Sacks.
     *
     * @see MacEditorKit#deletePrevWordAction
     * @see MacEditorKit#getActions
     */
    static class DeletePrevWordAction extends TextAction {
        private static final long serialVersionUID = -6352466583853318023L;

        /**
         * Creates this object with the appropriate identifier.
         */
        DeletePrevWordAction() {
            super(deletePrevWordAction);
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            boolean        beep   = true;

            if ((target != null) && (target.isEditable())) {
                int     offs   = target.getCaretPosition();
                boolean failed = false;

                try {
                    offs = Utilities.getPreviousWord(target, offs);
                } catch (BadLocationException bl) {

                    if (offs != 0) {
                        offs = 0;
                    } else {
                        failed = true;
                    }
                }

                if (!failed) {
                    target.moveCaretPosition(offs);

                    // and then delete it
                    target.replaceSelection("");
                    beep = false;
                }
            }

            if (beep) {
                provideErrorFeedback(target);
            }
        }
    }

    /**
     * Action to move the selection up or down. This is very similar to the
     * NextVisualPositionAction of class DefaultEditorKit. The differences is,
     * that we move the cursor to the beginning of the text, if the user wants
     * to move upwards and is already at the first line of the text. We move the
     * cursor to the end of the text, if the user wants to move downwards and is
     * already at the last line of the text. Note that we delegate actions to
     * DefaultEditorKit actions. We can not implement all the required code by
     * ourself, because method DefaultCaret.getDotBias() is not accessible from
     * outside the javax.swing.text package.
     */
    static class VerticalAction extends TextAction {
        private static final long serialVersionUID = -6471615785308538422L;
        private TextAction        verticalAction;
        private TextAction        beginEndAction;

        /**
         * Create this action with the appropriate identifier.
         *
         * @param name           DOCUMENT ME!
         * @param verticalAction DOCUMENT ME!
         * @param beginEndAction DOCUMENT ME!
         */
        VerticalAction(String name, TextAction verticalAction, TextAction beginEndAction) {
            super(name);
            this.verticalAction = verticalAction;
            this.beginEndAction = beginEndAction;
        }

        /**
         * The operation to perform when this action is triggered.
         *
         * @param e DOCUMENT ME!
         */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);

            if (target != null) {

                // target.getUI().getNextVisualPositionFrom(t
                Caret caret = target.getCaret();
                int   dot   = caret.getDot();

                verticalAction.actionPerformed(e);

                if (dot == caret.getDot()) {
                    Point magic = caret.getMagicCaretPosition();

                    beginEndAction.actionPerformed(e);
                    caret.setMagicCaretPosition(magic);
                }
            }
        }
    }
}
