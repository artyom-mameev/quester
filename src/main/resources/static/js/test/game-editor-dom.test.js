/**
 * @jest-environment jsdom
 */

const gameEditor_DomModule = require("../game-editor-dom");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpJquery();
    setUpGlobalVariables();
    setUpMocks();
    setUpTestData();
});

beforeEach(() => {
    setUpHtml();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpGlobalVariables() {
    global.DESC_INDEX = 0;
    global.COND_INDEX = 1;

    global.ADD_ROOM_STRING = "ADD_ROOM_STRING";
    global.ROOM_NAME_STRING = "ROOM_NAME_STRING";
    global.ROOM_DESC_STRING = "ROOM_DESC_STRING";
    global.ADD_CHOICE_STRING = "ADD_CHOICE_STRING";
    global.CHOICE_NAME_STRING = "CHOICE_NAME_STRING";
    global.FLAG_STRING = "FLAG_STRING";
    global.ADD_CONDITION_STRING = "ADD_CONDITION_STRING";
    global.EDIT_ROOM_STRING = "EDIT_ROOM_STRING";
    global.EDIT_CHOICE_STRING = "EDIT_CHOICE_STRING";
    global.EDIT_CONDITION_STRING = "EDIT_CONDITION_STRING";
    global.CREATE_A_NEW_GAME_STRING = "CREATE_A_NEW_GAME_STRING";
    global.GAME_NAME_STRING = "GAME_NAME_STRING";
    global.GAME_DESC_STRING = "GAME_DESC_STRING";
    global.ENGLISH_STRING = "ENGLISH_STRING";
    global.RUSSIAN_STRING = "RUSSIAN_STRING";
    global.UPDATE_GAME_INFO_STRING = "UPDATE_GAME_INFO_STRING";
    global.PUBLISH_STRING = "PUBLISH_STRING";

    global.FLAG_STATE = {
        FLAG_ACTIVE: "ACTIVE",
        FLAG_NOT_ACTIVE: "NOT_ACTIVE"
    }
}

function setUpTestData() {
    global.roomNode = {
        type: "ROOM",
        parent: "testRoomNodeParent",
        id: "testRoomNodeId",
        text: "testRoomNodeName",
        data: ["testRoomNodeDescription", null],
        children: [],
    }

    global.choiceNode = {
        type: "CHOICE",
        parent: "testChoiceNodeParent",
        id: "testChoiceNodeId",
        text: "testChoiceNodeName",
        data: [null, null],
        children: ["childId"],
    }

    global.conditionNode = {
        type: "CONDITION",
        parent: "testConditionNodeParent",
        id: "testConditionNodeId",
        text: "testConditionNodeName",
        data: [null, {
            flagId: "testFlagNodeId2",
            flagState: "modal-radio-condition-not-active"
        }],
        children: ["childId"],
    }

    global.flags = [{
        type: "FLAG",
        parent: "testFlagNodeParent",
        id: "testFlagNodeId",
        text: "testFlagNodeName",
        data: [null, null],
        children: [],
    },
        {
            type: "FLAG",
            parent: "testFlagNodeParent2",
            id: "testFlagNodeId2",
            text: "testFlagNodeName2",
            data: [null, null],
            children: [],
        }]
}

function setUpMocks() {
    jest.spyOn($.fn, 'show');
    jest.spyOn($.fn, 'off');
    jest.spyOn($.fn, 'on');
    jest.spyOn($.fn, 'click');

    $.fn.modal = jest.fn();

    $.fn.modal.prototype.constructor.Constructor = {
        Default: {
            backdrop: '',
            keyboard: true
        }
    }

    testUtils.mockVar("Option", window.Option);
}

function setUpHtml() {
    document.body.innerHTML = "<div>\n" +
        "    <div class=\"text-center\">\n" +
        "        <a id=\"createButton\" th:text=\"#{create.first-room}\"\n" +
        "           href=\"#modal_general\" data-toggle=\"modal\"\n" +
        "           class=\"btn btn-lg btn-secondary fw-bold border-white bg-white\"></a>\n" +
        "    </div>\n" +
        "\n" +
        "    <div id=\"modal_general\" class=\"modal\">\n" +
        "    <div class=\"modal-dialog\">\n" +
        "        <div class=\"modal-content\">\n" +
        "            <div class=\"modal-header\">\n" +
        "                <h5 id=\"modal-h5-title\" class=\"modal-title\"></h5>\n" +
        "                <button type=\"button\" class=\"close\" data-dismiss=\"modal\"><span aria-hidden=\"true\">&times;</span><span\n" +
        "                        class=\"sr-only\"></span></button>\n" +
        "            </div>\n" +
        "\n" +
        "            <div class=\"modal-body\">\n" +
        "                    <label for=\"modal-input-name\" id=\"modal-label-name\" class=\"col-form-label\"></label>\n" +
        "                    <input id=\"modal-input-name\" type=\"text\" value=\"test\">\n" +
        "                    <div><label id=\"modal-name-error\" hidden=\"true\" class=\"error-text\"></label></div>\n" +
        "                    <label for=\"modal-textarea-description\" id=\"modal-label-description\" class=\"col-form-label modal-element\"></label>\n" +
        "                    <textarea id=\"modal-textarea-description\" class=\"form-control margin-bottom-1\"></textarea>\n" +
        "                    <div><label id=\"modal-desc-error\" hidden=\"true\" class=\"error-text\"></label></div>\n" +
        "\n" +
        "                    <dd class=\"modal-element\" id=\"modal-dd-checkbox\">\n" +
        "                            <input id=\"modal-checkbox\" th:text=\"' ' + #{create.flag}\" type=\"checkbox\" value=\"1\"\n" +
        "                                   name=\"modal-checkbox[]\">\n" +
        "                        <label id=\"modal-checkbox-label\" for=\"modal-checkbox\"></label>\n" +
        "                    </dd>\n" +
        "                    <select id=\"modal-select\"></select>\n" +
        "\n" +
        "                    <input class=\"modal-radio\" id=\"modal-radio-condition-active\" name=\"modal-radio-condition\"\n" +
        "                           type=\"radio\" value=\"modal-radio-condition-active\">\n" +
        "                    <label id=\"modal-radio-label-active\" for=\"modal-radio-condition-active\"\n" +
        "                           th:text=\"#{create.active}\"></label>\n" +
        "                    <input id=\"modal-radio-condition-not-active\" name=\"modal-radio-condition\"\n" +
        "                           type=\"radio\" value=\"modal-radio-condition-not-active\">\n" +
        "                    <label for=\"modal-radio-condition-not-active\" id=\"modal-radio-label-not-active\"\n" +
        "                           th:text=\"#{create.not-active}\"></label>\n" +
        "                    <div><label id=\"modal-condition-error\" hidden=\"true\" class=\"error-text\"></label></div>\n" +
        "            </div>\n" +
        "            <div class=\"modal-footer\">\n" +
        "                <button id=\"modal-button-save\" th:text=\"#{create.save}\"\n" +
        "                        type=\"button\" class=\"btn btn-light\">\n" +
        "                </button>\n" +
        "                <button th:text=\"#{create.close}\" type=\"button\" class=\"btn btn-secondary\"\n" +
        "                        data-dismiss=\"modal\"></button>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "</div>\n" +
        "\n" +
        "<div id=\"jstree\"></div>\n" +
        "    <button id=\"edit-game-info-button\" style=\"width: 30px\" type=\"button\"\n" +
        "            class=\"btn btn-light\">Edit Game</button>\n" +
        "</div>\n";
}

test('prepareModalForAddingRoomNode changes modal title', () => {
    gameEditor_DomModule.prepareModalForAddingRoomNode();

    expect(modalTitleIs(ADD_ROOM_STRING)).toBeTruthy();
});

test('prepareModalForAddingRoomNode changes modal name label', () => {
    gameEditor_DomModule.prepareModalForAddingRoomNode();

    expect(modalNameLabelIs(ROOM_NAME_STRING)).toBeTruthy();
});

test('prepareModalForAddingRoomNode shows name edit in modal', () => {
    gameEditor_DomModule.prepareModalForAddingRoomNode();

    expect(nameEditIsShown()).toBeTruthy();
});

test('prepareModalForAddingRoomNode shows description edit in modal', () => {
    gameEditor_DomModule.prepareModalForAddingRoomNode();

    expect(descriptionEditIsShown()).toBeTruthy();
});

test('prepareModalForAddingRoomNode changes modal description label', () => {
    gameEditor_DomModule.prepareModalForAddingRoomNode();

    expect(modalDescLabelIs(ROOM_DESC_STRING)).toBeTruthy();
});

test('prepareModalForAddingRoomNode hides checkbox in modal', () => {
    gameEditor_DomModule.prepareModalForAddingRoomNode();

    expect(flagCheckboxIsShown()).toBeFalsy();
});

test('prepareModalForAddingRoomNode hides select in modal', () => {
    gameEditor_DomModule.prepareModalForAddingRoomNode();

    expect(selectIsShown()).toBeFalsy();
});

test('prepareModalForAddingRoomNode hides radio buttons in modal', () => {
    gameEditor_DomModule.prepareModalForAddingRoomNode();

    expect(radioButtonsIsShown()).toBeFalsy();
});

test('prepareModalForAddingChoiceNode changes modal title', () => {
    gameEditor_DomModule.prepareModalForAddingChoiceNode();

    expect(modalTitleIs(ADD_CHOICE_STRING)).toBeTruthy();
});

test('prepareModalForAddingChoiceNode changes modal name label', () => {
    gameEditor_DomModule.prepareModalForAddingChoiceNode();

    expect(modalNameLabelIs(CHOICE_NAME_STRING)).toBeTruthy();
});

test('prepareModalForAddingChoiceNode shows name edit in modal', () => {
    gameEditor_DomModule.prepareModalForAddingChoiceNode();

    expect(nameEditIsShown()).toBeTruthy();
});

test('prepareModalForAddingChoiceNode hides description edit in modal', () => {
    gameEditor_DomModule.prepareModalForAddingChoiceNode();

    expect(descriptionEditIsShown()).toBeFalsy();
});

test('prepareModalForAddingChoiceNode shows checkbox in modal', () => {
    gameEditor_DomModule.prepareModalForAddingChoiceNode();

    expect(flagCheckboxIsShown()).toBeTruthy();
});

test('prepareModalForAddingChoiceNode sets checkbox name in modal', () => {
    gameEditor_DomModule.prepareModalForAddingChoiceNode();

    expect(modalCheckboxNameIs(FLAG_STRING)).toBeTruthy();
});

test('prepareModalForAddingChoiceNode hides select in modal', () => {
    gameEditor_DomModule.prepareModalForAddingChoiceNode();

    expect(selectIsShown()).toBeFalsy();
});

test('prepareModalForAddingChoiceNode hides radio buttons in modal', () => {
    gameEditor_DomModule.prepareModalForAddingChoiceNode();

    expect(radioButtonsIsShown()).toBeFalsy();
});

test('prepareModalForAddingConditionNode changes modal title', () => {
    gameEditor_DomModule.prepareModalForAddingConditionNode(flags);

    expect(modalTitleIs(ADD_CONDITION_STRING)).toBeTruthy();
});

test('prepareModalForAddingConditionNode hides name edit in modal', () => {
    gameEditor_DomModule.prepareModalForAddingConditionNode(flags);

    expect(nameEditIsShown()).toBeFalsy();
});

test('prepareModalForAddingConditionNode hides description edit in modal', () => {
    gameEditor_DomModule.prepareModalForAddingConditionNode(flags);

    expect(descriptionEditIsShown()).toBeFalsy();
});

test('prepareModalForAddingConditionNode hides checkbox in modal', () => {
    gameEditor_DomModule.prepareModalForAddingConditionNode(flags);

    expect(flagCheckboxIsShown()).toBeFalsy();
});

test('prepareModalForAddingConditionNode adds flags to modal select', () => {
    gameEditor_DomModule.prepareModalForAddingConditionNode(flags);

    const options = getModalSelectOptions();

    expect(options[0].name).toEqual("testFlagNodeName");
    expect(options[0].value).toEqual("testFlagNodeId");
    expect(options[1].name).toEqual("testFlagNodeName2");
    expect(options[1].value).toEqual("testFlagNodeId2");
});

test('prepareModalForAddingConditionNode shows select in modal', () => {
    gameEditor_DomModule.prepareModalForAddingConditionNode(flags);

    expect(selectIsShown()).toBeTruthy();
});

test('prepareModalForAddingConditionNode shows radio buttons in modal', () => {
    gameEditor_DomModule.prepareModalForAddingConditionNode(flags);

    expect(radioButtonsIsShown()).toBeTruthy();
});

test('prepareModalForEditingRoomNode changes modal title', () => {
    gameEditor_DomModule.prepareModalForEditingRoomNode(roomNode);

    expect(modalTitleIs(EDIT_ROOM_STRING)).toBeTruthy();
});

test('prepareModalForEditingRoomNode changes modal name label', () => {
    gameEditor_DomModule.prepareModalForEditingRoomNode(roomNode);

    expect(modalNameLabelIs(ROOM_NAME_STRING)).toBeTruthy();
});

test('prepareModalForEditingRoomNode shows name edit in modal', () => {
    gameEditor_DomModule.prepareModalForEditingRoomNode(roomNode);

    expect(nameEditIsShown()).toBeTruthy();
});

test('prepareModalForEditingRoomNode shows description edit in modal', () => {
    gameEditor_DomModule.prepareModalForEditingRoomNode(roomNode);

    expect(descriptionEditIsShown()).toBeTruthy();
});

test('prepareModalForEditingRoomNode changes modal description label', () => {
    gameEditor_DomModule.prepareModalForEditingRoomNode(roomNode);

    expect(modalDescLabelIs(ROOM_DESC_STRING)).toBeTruthy();
});

test('prepareModalForEditingRoomNode hides checkbox in modal', () => {
    gameEditor_DomModule.prepareModalForEditingRoomNode(roomNode);

    expect(flagCheckboxIsShown()).toBeFalsy();
});

test('prepareModalForEditingRoomNode changes modal name value', () => {
    gameEditor_DomModule.prepareModalForEditingRoomNode(roomNode);

    expect(getModalName()).toEqual(roomNode.text);
});

test('prepareModalForEditingRoomNode changes modal description value', () => {
    gameEditor_DomModule.prepareModalForEditingRoomNode(roomNode);

    expect(getModalDesc()).toEqual(roomNode.data[DESC_INDEX]);
});


test('prepareModalForEditingRoomNode hides select in modal', () => {
    gameEditor_DomModule.prepareModalForEditingRoomNode(roomNode);

    expect(selectIsShown()).toBeFalsy();
});

test('prepareModalForEditingRoomNode hides radio buttons in modal', () => {
    gameEditor_DomModule.prepareModalForEditingRoomNode(roomNode);

    expect(radioButtonsIsShown()).toBeFalsy();
});


test('prepareModalForEditingChoiceNode changes modal title', () => {
    gameEditor_DomModule.prepareModalForEditingChoiceNode(choiceNode);

    expect(modalTitleIs(EDIT_CHOICE_STRING)).toBeTruthy();
});

test('prepareModalForEditingChoiceNode changes modal name label', () => {
    gameEditor_DomModule.prepareModalForEditingChoiceNode(choiceNode);

    expect(modalNameLabelIs(CHOICE_NAME_STRING)).toBeTruthy();
});

test('prepareModalForEditingChoiceNode shows name edit in modal', () => {
    gameEditor_DomModule.prepareModalForEditingChoiceNode(choiceNode);

    expect(nameEditIsShown()).toBeTruthy();
});

test('prepareModalForEditingChoiceNode adds gap after name edit in modal', () => {
    gameEditor_DomModule.prepareModalForEditingChoiceNode(choiceNode);

    expect(gapAfterNameEditIsShown()).toBeTruthy();
});

test('prepareModalForEditingChoiceNode hides description edit in modal', () => {
    gameEditor_DomModule.prepareModalForEditingChoiceNode(choiceNode);

    expect(descriptionEditIsShown()).toBeFalsy();
});

test('prepareModalForEditingChoiceNode hides checkbox in modal', () => {
    gameEditor_DomModule.prepareModalForEditingChoiceNode(choiceNode);

    expect(flagCheckboxIsShown()).toBeFalsy();
});

test('prepareModalForEditingChoiceNode changes modal name value', () => {
    gameEditor_DomModule.prepareModalForEditingChoiceNode(choiceNode);

    expect(getModalName()).toEqual(choiceNode.text);
});

test('prepareModalForEditingChoiceNode hides select in modal', () => {
    gameEditor_DomModule.prepareModalForEditingChoiceNode(choiceNode);

    expect(selectIsShown()).toBeFalsy();
});

test('prepareModalForEditingChoiceNode hides radio buttons in modal', () => {
    gameEditor_DomModule.prepareModalForEditingChoiceNode(choiceNode);

    expect(radioButtonsIsShown()).toBeFalsy();
});

test('prepareModalForEditingConditionNode changes modal title', () => {
    gameEditor_DomModule.prepareModalForEditingConditionNode(conditionNode,
        flags);

    expect(modalTitleIs(EDIT_CONDITION_STRING)).toBeTruthy();
});

test('prepareModalForEditingConditionNode hides name edit in modal', () => {
    gameEditor_DomModule.prepareModalForEditingConditionNode(conditionNode,
        flags);

    expect(nameEditIsShown()).toBeFalsy();
});

test('prepareModalForEditingConditionNode hides description edit in modal', () => {
    gameEditor_DomModule.prepareModalForEditingConditionNode(conditionNode,
        flags);

    expect(descriptionEditIsShown()).toBeFalsy();
});

test('prepareModalForEditingConditionNode hides checkbox in modal', () => {
    gameEditor_DomModule.prepareModalForEditingConditionNode(conditionNode,
        flags);

    expect(flagCheckboxIsShown()).toBeFalsy();
});

test('prepareModalForEditingConditionNode shows select in modal', () => {
    gameEditor_DomModule.prepareModalForEditingConditionNode(conditionNode,
        flags);

    expect(selectIsShown()).toBeTruthy();
});

test('prepareModalForEditingConditionNode shows radio buttons in modal', () => {
    gameEditor_DomModule.prepareModalForEditingConditionNode(conditionNode,
        flags);

    expect(radioButtonsIsShown()).toBeTruthy();
});

test('prepareModalForEditingConditionNode adds flags to modal select', () => {
    gameEditor_DomModule.prepareModalForEditingConditionNode(conditionNode,
        flags);

    const options = getModalSelectOptions();

    expect(options[0].name).toEqual("testFlagNodeName");
    expect(options[0].value).toEqual("testFlagNodeId");
    expect(options[1].name).toEqual("testFlagNodeName2");
    expect(options[1].value).toEqual("testFlagNodeId2");
});

test('prepareModalForEditingConditionNode changes modal radio value', () => {
    gameEditor_DomModule.prepareModalForEditingConditionNode(conditionNode,
        flags);

    expect(getSelectValueFromModal()).toEqual("testFlagNodeId2");
    expect(isRadioActiveChecked()).toBeFalsy();
    expect(isRadioNotActiveChecked()).toBeTruthy();
});

test('prepareModalForCreatingGame changes modal title', () => {
    gameEditor_DomModule.prepareModalForCreatingGame();

    expect(modalTitleIs(CREATE_A_NEW_GAME_STRING)).toBeTruthy();
});

test('prepareModalForCreatingGame changes modal name label', () => {
    gameEditor_DomModule.prepareModalForCreatingGame();

    expect(modalNameLabelIs(GAME_NAME_STRING)).toBeTruthy();
});

test('prepareModalForCreatingGame shows name edit in modal', () => {
    gameEditor_DomModule.prepareModalForCreatingGame();

    expect(nameEditIsShown()).toBeTruthy();
});

test('prepareModalForCreatingGame shows description edit in modal', () => {
    gameEditor_DomModule.prepareModalForCreatingGame();

    expect(descriptionEditIsShown()).toBeTruthy();
});

test('prepareModalForCreatingGame changes modal description label', () => {
    gameEditor_DomModule.prepareModalForCreatingGame();

    expect(modalDescLabelIs(GAME_DESC_STRING)).toBeTruthy();
});

test('prepareModalForCreatingGame hides checkbox in modal', () => {
    gameEditor_DomModule.prepareModalForCreatingGame();

    expect(flagCheckboxIsShown()).toBeFalsy();
});

test('prepareModalForCreatingGame shows select in modal', () => {
    gameEditor_DomModule.prepareModalForCreatingGame();

    expect(selectIsShown()).toBeTruthy();
});

test('prepareModalForCreatingGame hides radio buttons in modal', () => {
    gameEditor_DomModule.prepareModalForCreatingGame();

    expect(radioButtonsIsShown()).toBeFalsy();
});

test('prepareModalForCreatingGame adds modal select options', () => {
    gameEditor_DomModule.prepareModalForCreatingGame();

    const options = getModalSelectOptions();

    expect(options[0].name).toEqual(ENGLISH_STRING);
    expect(options[0].value).toEqual(ENGLISH_STRING);
    expect(options[1].name).toEqual(RUSSIAN_STRING);
    expect(options[1].value).toEqual(RUSSIAN_STRING);
});

test('prepareModalForUpdatingGameInfo changes modal title', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(modalTitleIs(UPDATE_GAME_INFO_STRING)).toBeTruthy();
});

test('prepareModalForUpdatingGameInfo shows name edit in modal', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(nameEditIsShown()).toBeTruthy();
});

test('prepareModalForUpdatingGameInfo changes modal name label', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(modalNameLabelIs(GAME_NAME_STRING)).toBeTruthy();
});

test('prepareModalForUpdatingGameInfo changes modal name value', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(getModalName()).toEqual("gameName");
});

test('prepareModalForUpdatingGameInfo shows description edit in modal', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(descriptionEditIsShown()).toBeTruthy();
});

test('prepareModalForUpdatingGameInfo changes modal description label', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(modalDescLabelIs(GAME_DESC_STRING)).toBeTruthy();
});

test('prepareModalForUpdatingGameInfo changes modal description value', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(getModalDesc()).toEqual("gameDesc");
});

test('prepareModalForUpdatingGameInfo shows select in modal', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(selectIsShown()).toBeTruthy();
});

test('prepareModalForUpdatingGameInfo adds modal select options', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    const options = getModalSelectOptions();

    expect(options[0].name).toEqual(ENGLISH_STRING);
    expect(options[0].value).toEqual(ENGLISH_STRING);
    expect(options[1].name).toEqual(RUSSIAN_STRING);
    expect(options[1].value).toEqual(RUSSIAN_STRING);
});

test('prepareModalForUpdatingGameInfo changes modal select value', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(getSelectValueFromModal()).toEqual(ENGLISH_STRING);
});


test('prepareModalForUpdatingGameInfo shows checkbox in modal', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(flagCheckboxIsShown()).toBeTruthy();
});

test('prepareModalForUpdatingGameInfo sets checkbox name in modal', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(modalCheckboxNameIs(PUBLISH_STRING)).toBeTruthy();
});

test('prepareModalForUpdatingGameInfo hides radio buttons in modal', () => {
    gameEditor_DomModule.prepareModalForUpdatingGameInfo("gameName",
        "gameDesc", ENGLISH_STRING);

    expect(radioButtonsIsShown()).toBeFalsy();
});

test('showEditInfoButton shows edit info button', () => {
    gameEditor_DomModule.showEditInfoButton();

    expect($("#edit-game-info-button:visible")).toBeTruthy();
});

test('hideEditInfoButton hides edit info button', () => {
    gameEditor_DomModule.hideEditInfoButton();

    expect($("#edit-game-info-button:hidden")).toBeTruthy();
});

test('showModal shows modal', () => {
    gameEditor_DomModule.showModal(emptyFunc);

    expect($.fn.modal).toHaveBeenCalledTimes(1);
});

test('showModal sets modal that on show function should be ran', () => {
    const emptyMock = jest.fn();
    gameEditor_DomModule.showModal(emptyMock);

    const event = $.Event("show.bs.modal");
    $("#modal_general").trigger(event);

    expect(emptyMock).toHaveBeenCalledTimes(1);
});

test('showModal sets modal that on hide modal should be cleaned', () => {
    gameEditor_DomModule.showModal(jest.fn());

    const modalRadioCondition = $('input[name="modal-radio-condition"]');

    $('#modal-input-name').val('test');
    $('#modal-textarea-description').val('test');
    $('input[type="checkbox"]').prop('checked', true);
    $('#modal-select').append(new Option("testName", "testValue"));
    modalRadioCondition.prop('checked', true);
    $('#modal-name-error').removeAttr("hidden");
    $('#modal-desc-error').removeAttr("hidden");
    $('#modal-condition-error').removeAttr("hidden");
    $("#modal-textarea-description").removeClass('margin-bottom-1');
    $("#modal-input-name").addClass('margin-bottom-1');
    $('#modal_general .close').css('visibility', 'hidden');
    $('#modal-button-close').hide();
    // noinspection JSPotentiallyInvalidConstructorUsage,JSUnresolvedVariable
    $.fn.modal.prototype.constructor.Constructor.Default.backdrop = 'static';
    // noinspection JSPotentiallyInvalidConstructorUsage,JSUnresolvedVariable
    $.fn.modal.prototype.constructor.Constructor.Default.keyboard = false;

    const event = $.Event("hidden.bs.modal");
    $("#modal_general").trigger(event);

    expect(getModalName()).toEqual('');
    expect(getModalDesc()).toEqual('');
    expect(isModalCheckboxChecked()).toBeFalsy();
    expect(isModalSelectEmpty()).toBeTruthy();
    expect(modalRadioCondition.is(':checked')).toBeFalsy();
    expect(isModalNameErrorHidden()).toBeTruthy();
    expect(isModalDescErrorHidden()).toBeTruthy();
    expect(isModalConditionErrorHidden()).toBeTruthy();
    expect(gapAfterDescEditIsShown()).toBeTruthy();
    expect(gapAfterNameEditIsShown()).toBeFalsy();
    expect(isModalClosingPrevented()).toBeFalsy();
});

test('hideModal hides modal', () => {
    gameEditor_DomModule.hideModal();

    expect($.fn.modal).toHaveBeenCalledTimes(1);
    expect($.fn.modal).toHaveBeenCalledWith('hide');
});

test('preventClosingModal prevents modal closing', () => {
    gameEditor_DomModule.preventClosingModal();

    expect(isModalClosingPrevented()).toBeTruthy();
});

test('setModalSaveButtonOnClick sets on click event to save button in modal', () => {
    const emptyFunc = jest.fn();
    gameEditor_DomModule.setModalSaveButtonOnClick(emptyFunc);

    $('#modal-button-save').trigger('click');

    expect(emptyFunc).toHaveBeenCalledTimes(1);
});

test('getNameValueFromModal gets name value from modal', () => {
    $('#modal-input-name').val('test')

    expect(gameEditor_DomModule.getNameValueFromModal()).toEqual("test");
});

test('getDescValueFromModal gets description value from modal', () => {
    $('#modal-textarea-description').val('test')

    expect(gameEditor_DomModule.getDescValueFromModal()).toEqual("test");
});

test('isModalCheckboxChecked returns true if modal checkbox is checked', () => {
    $('#modal-checkbox').attr('checked', true);
    expect(gameEditor_DomModule.isModalCheckboxChecked()).toBeTruthy();
});

test('isModalCheckboxChecked returns false if modal checkbox is not checked', () => {
    $('#modal-checkbox').attr('checked', false);
    expect(gameEditor_DomModule.isModalCheckboxChecked()).toBeFalsy();
});

test('getSelectValueFromModal gets select value from modal', () => {
    $('#modal-select').append(new Option("testName", "testValue"));
    $('#modal-select option[value=testValue]').prop('selected', true);
    expect(gameEditor_DomModule.getSelectValueFromModal()).toBeTruthy();
});

test('getModalRadioValue gets modal radio value if active', () => {
    $("input[name=modal-radio-condition][value='modal-radio-condition-active']")
        .prop("checked", true);
    expect(gameEditor_DomModule.getModalRadioValue())
        .toEqual(FLAG_STATE.FLAG_ACTIVE);
});

test('getModalRadioValue gets modal radio value if not active', () => {
    $("input[name=modal-radio-condition][value='modal-radio-condition-not-active']")
        .prop("checked", true);
    expect(gameEditor_DomModule.getModalRadioValue())
        .toEqual(FLAG_STATE.FLAG_NOT_ACTIVE);
});

test('clearErrorsInModal clears errors in modal', () => {
    gameEditor_DomModule.clearErrorsInModal();

    expect(isModalNameErrorHidden()).toBeTruthy();
    expect(isModalDescErrorHidden()).toBeTruthy();
    expect(isModalConditionErrorHidden()).toBeTruthy();
});

test('showModalFieldErrors shows name constraint errors', () => {
    const nameFieldErrors = [{
        field: "name",
        defaultMessage: "testError1"
    }];

    gameEditor_DomModule.showModalFieldErrors(nameFieldErrors);

    const nameFieldErrors2 = [{
        field: "name",
        defaultMessage: "testError2"
    }];

    gameEditor_DomModule.showModalFieldErrors(nameFieldErrors2);

    expect(testUtils.hasAttr("modal-name-error", "hidden")).toBeFalsy();
    expect($('#modal-name-error').html())
        .toEqual("testError1<br>testError2");
});

test('showModalFieldErrors shows description constraint errors', () => {
    const descFieldErrors = [{
        field: "description",
        defaultMessage: "testError1"
    }];

    gameEditor_DomModule.showModalFieldErrors(descFieldErrors);

    const descFieldErrors2 = [{
        field: "description",
        defaultMessage: "testError2"
    }];

    gameEditor_DomModule.showModalFieldErrors(descFieldErrors2);

    expect(testUtils.hasAttr("modal-desc-error", "hidden")).toBeFalsy();
    expect($('#modal-desc-error').html())
        .toEqual("testError1<br>testError2");
});

test('showModalFieldErrors adds gap after description edit in modal', () => {
    const descFieldErrors = [{
        field: "description",
        defaultMessage: "testError1"
    }];

    gameEditor_DomModule.showModalFieldErrors(descFieldErrors);

    expect($("#modal-textarea-description").hasClass('margin-bottom-1'))
        .toBeFalsy();
});

test('showModalFieldErrors shows condition constraint errors', () => {
    const conditionFieldErrors = [{
        field: "condition.flagState",
        defaultMessage: "testError1"
    }];

    gameEditor_DomModule.showModalFieldErrors(conditionFieldErrors);

    const conditionFieldErrors2 = [{
        field: "condition.flagState",
        defaultMessage: "testError2"
    }];

    gameEditor_DomModule.showModalFieldErrors(conditionFieldErrors2);

    expect(testUtils.hasAttr("modal-condition-error", "hidden"))
        .toBeFalsy();
    expect($('#modal-condition-error').html())
        .toEqual("testError1<br>testError2");
});

test('showPlayButton shows play button', () => {
    gameEditor_DomModule.hidePlayButton();

    gameEditor_DomModule.showPlayButton();

    expect($("#play-button:visible")).toBeTruthy();
});

test('hidePlayButton hides play button', () => {
    gameEditor_DomModule.hidePlayButton();

    expect($("#play-button:hidden")).toBeTruthy();
});

function modalTitleIs(text) {
    return $('#modal-h5-title').text() === text;
}

function modalNameLabelIs(text) {
    return $('#modal-label-name').text() === text;
}

function modalDescLabelIs(text) {
    return $('#modal-label-description').text() === text;
}

function modalCheckboxNameIs(text) {
    return $('#modal-checkbox-label').text() === text;
}

function isModalCheckboxChecked() {
    return $('#modal-checkbox').is(':checked');
}

function isModalSelectEmpty() {
    return $('#modal-select').is(':empty');
}

function isRadioActiveChecked() {
    return $('#modal-radio-condition-active').is(':checked');
}

function isRadioNotActiveChecked() {
    return $('#modal-radio-condition-not-active').is(':checked');
}

function isModalNameErrorHidden() {
    return testUtils.hasAttr("modal-name-error", "hidden");
}

function isModalDescErrorHidden() {
    return testUtils.hasAttr("modal-desc-error", "hidden");
}

function isModalConditionErrorHidden() {
    return testUtils.hasAttr("modal-condition-error", "hidden");
}

function nameEditIsShown() {
    return $('#modal-label-name').css("display") !== 'none' &&
        $('#modal-input-name').css("display") !== 'none'
}

function descriptionEditIsShown() {
    return $('#modal-label-description').css("display") !== 'none' &&
        $('#modal-textarea-description').css("display") !== 'none'

}

function flagCheckboxIsShown() {
    return $('#modal-dd-checkbox').css("display") !== 'none';
}

function selectIsShown() {
    return $('#modal-select').css("display") !== 'none';
}

function radioButtonsIsShown() {
    return $('input[name="modal-radio-condition"]').css("display") !== 'none' &&
        $('#modal-radio-label-active').css("display") !== 'none' &&
        $('#modal-radio-label-not-active').css("display") !== 'none';
}

function gapAfterNameEditIsShown() {
    return $("#modal-input-name").hasClass('margin-bottom-1');
}

function gapAfterDescEditIsShown() {
    return $("#modal-textarea-description").hasClass('margin-bottom-1');
}

function getModalName() {
    return $('#modal-input-name').val();
}

function getModalDesc() {
    return $('#modal-textarea-description').val();
}

function getSelectValueFromModal() {
    return $('#modal-select option:selected').val();
}

function getModalSelectOptions() {
    return $.map($('#modal-select option'), function (option) {
        return {
            name: option.label,
            value: option.value
        };
    });
}

function isModalClosingPrevented() {
    // noinspection JSUnresolvedVariable,JSUnresolvedVariable
    return $('#modal_general .close').css('visibility') === 'hidden' &&
        $('#modal-button-close:hidden') &&
        $.fn.modal.prototype.constructor.Constructor.Default
            .backdrop === 'static' &&
        $.fn.modal.prototype.constructor.Constructor.Default
            .keyboard === false;
}

function emptyFunc() {

}