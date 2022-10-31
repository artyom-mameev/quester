function prepareModalForAddingRoomNode() {
    changeModalTitle(ADD_ROOM_STRING);
    changeModalNameLabel(ROOM_NAME_STRING);
    showNameEditInModal();
    showDescriptionEditInModal();
    changeModalDescLabel(ROOM_DESC_STRING);
    hideCheckboxInModal();
    hideSelectInModal();
    hideRadioButtonsInModal();
}

function prepareModalForAddingChoiceNode() {
    changeModalTitle(ADD_CHOICE_STRING);
    changeModalNameLabel(CHOICE_NAME_STRING);
    showNameEditInModal();
    hideDescriptionEditInModal();
    showCheckboxInModal();
    setCheckboxNameInModal(FLAG_STRING)
    hideSelectInModal();
    hideRadioButtonsInModal();
}

function prepareModalForAddingConditionNode(flags) {
    changeModalTitle(ADD_CONDITION_STRING);
    hideNameEditInModal();
    hideDescriptionEditInModal();
    hideCheckboxInModal();
    addFlagsToModalSelect(flags);
    showSelectInModal();
    showRadioButtonsInModal();
}

function prepareModalForEditingRoomNode(node) {
    changeModalTitle(EDIT_ROOM_STRING);
    changeModalNameLabel(ROOM_NAME_STRING);
    showNameEditInModal();
    showDescriptionEditInModal();
    changeModalDescLabel(ROOM_DESC_STRING);
    hideCheckboxInModal();
    changeModalNameValue(node.text);
    changeModalDescriptionValue(node.data[DESC_INDEX]);
    hideSelectInModal();
    hideRadioButtonsInModal();
}

function prepareModalForEditingChoiceNode(node) {
    changeModalTitle(EDIT_CHOICE_STRING);
    changeModalNameLabel(CHOICE_NAME_STRING)
    showNameEditInModal();
    addGapAfterNameEditInModal();
    hideDescriptionEditInModal();
    hideCheckboxInModal();
    changeModalNameValue(node.text);
    hideSelectInModal();
    hideRadioButtonsInModal();
}

function prepareModalForEditingConditionNode(node, flags) {
    changeModalTitle(EDIT_CONDITION_STRING)
    hideNameEditInModal();
    hideDescriptionEditInModal();
    hideCheckboxInModal();
    showSelectInModal();
    showRadioButtonsInModal();
    addFlagsToModalSelect(flags);
    changeModalSelectValue(node.data[COND_INDEX].flagId);
    changeModalRadioState(node.data[COND_INDEX].flagState, true)
}

function prepareModalForCreatingGame() {
    changeModalTitle(CREATE_A_NEW_GAME_STRING);
    changeModalNameLabel(GAME_NAME_STRING);
    showNameEditInModal();
    showDescriptionEditInModal();
    changeModalDescLabel(GAME_DESC_STRING);
    hideCheckboxInModal();
    showSelectInModal();
    hideRadioButtonsInModal();
    addModalSelectOptions(
        {optionName: ENGLISH_STRING, optionValue: ENGLISH_STRING},
        {optionName: RUSSIAN_STRING, optionValue: RUSSIAN_STRING});
}

function prepareModalForUpdatingGameInfo(gameName, gameDesc, gameLang) {
    changeModalTitle(UPDATE_GAME_INFO_STRING);
    showNameEditInModal();
    changeModalNameLabel(GAME_NAME_STRING);
    changeModalNameValue(gameName);
    showDescriptionEditInModal();
    changeModalDescLabel(GAME_DESC_STRING);
    changeModalDescriptionValue(gameDesc);
    showSelectInModal();
    addModalSelectOptions(
        {optionName: ENGLISH_STRING, optionValue: ENGLISH_STRING},
        {optionName: RUSSIAN_STRING, optionValue: RUSSIAN_STRING});
    changeModalSelectValue(gameLang);
    showCheckboxInModal();
    setCheckboxNameInModal(PUBLISH_STRING);
    hideRadioButtonsInModal();
}

function showModalFieldErrors(fieldErrors) {
    for (let error of fieldErrors) {
        switch (error.field) {
            case "name":
                addConstraintErrorToModal($('#modal-name-error'),
                    error.defaultMessage)
                break;

            case "description":
                removeGapAfterDescriptionInputInModal();

                addConstraintErrorToModal($('#modal-desc-error'),
                    error.defaultMessage)
                break;

            case "condition.flagState":
                addConstraintErrorToModal($('#modal-condition-error'),
                    error.defaultMessage)
                break;
        }
    }
}

function changeModalTitle(string) {
    $('#modal-h5-title').text(string);
}

function changeModalNameLabel(string) {
    $('#modal-label-name').text(string);
}

function changeModalDescLabel(string) {
    $('#modal-label-description').text(string);
}

function showNameEditInModal() {
    $('#modal-label-name').show();
    $('#modal-input-name').show();
}

function hideNameEditInModal() {
    $('#modal-label-name').hide();
    $('#modal-input-name').hide();
}

function showDescriptionEditInModal() {
    $('#modal-label-description').show();
    $('#modal-textarea-description').show();
}

function hideDescriptionEditInModal() {
    $('#modal-label-description').hide();
    $('#modal-textarea-description').hide();
}

function addGapAfterNameEditInModal() {
    $("#modal-input-name").addClass('margin-bottom-1');
}

function removeGapAfterNameEditInModal() {
    $("#modal-input-name").removeClass('margin-bottom-1');
}

function addGapAfterDescriptionEditInModal() {
    $("#modal-textarea-description").addClass('margin-bottom-1');
}

function removeGapAfterDescriptionInputInModal() {
    $("#modal-textarea-description").removeClass('margin-bottom-1');
}

function showCheckboxInModal() {
    $('#modal-dd-checkbox').show();
}

function hideCheckboxInModal() {
    $('#modal-dd-checkbox').hide();
}

function setCheckboxNameInModal(name) {
    $('#modal-checkbox-label').text(name);
}

function showSelectInModal() {
    $('#modal-select').show();
}

function hideSelectInModal() {
    $('#modal-select').hide()
}

function changeModalSelectValue(id) {
    $("#modal-select").val(id).change();
}

function addFlagsToModalSelect(flags) {
    for (let flag of flags) {
        addModalSelectOptions({
            optionName: flag.text,
            optionValue: flag.id
        });
    }
}

function addModalSelectOptions(...options) {
    for (let option of options) {
        $('#modal-select').append(new Option(
            option.optionName, option.optionValue));
    }
}

function showRadioButtonsInModal() {
    $('input[name="modal-radio-condition"]').show();
    $('#modal-radio-label-active').show();
    $('#modal-radio-label-not-active').show();
}

function hideRadioButtonsInModal() {
    $('input[name="modal-radio-condition"]').hide();
    $('#modal-radio-label-active').hide();
    $('#modal-radio-label-not-active').hide();
}

function changeModalRadioState(flagState, checked) {
    flagState = flagState === FLAG_STATE.FLAG_ACTIVE ?
        "modal-radio-condition-active" : "modal-radio-condition-not-active";

    $("#" + flagState).prop("checked", checked);
}

function clearElementsInModal() {
    $('#modal-input-name').val('');
    $('#modal-textarea-description').val('');
    $('input[type="checkbox"]').prop('checked', false);
    $('#modal-select').empty();
    $('input[name="modal-radio-condition"]').prop('checked', false);
    $('#modal-name-error').attr("hidden", "true");
    $('#modal-desc-error').attr("hidden", "true");
    $('#modal-condition-error').attr("hidden", "true");

    addGapAfterDescriptionEditInModal();
    removeGapAfterNameEditInModal();
    allowClosingModal();
}

function getNameValueFromModal() {
    return $('#modal-input-name').val().trim();
}

function getDescValueFromModal() {
    return $('#modal-textarea-description').val().trim();
}

function getSelectValueFromModal() {
    return $('#modal-select').find(":selected").val();
}

function getModalRadioValue() {
    const value = $('input[name="modal-radio-condition"]:checked').val();

    return value === "modal-radio-condition-active" ? FLAG_STATE.FLAG_ACTIVE :
        FLAG_STATE.FLAG_NOT_ACTIVE;
}

function isModalCheckboxChecked() {
    return $('#modal-checkbox').is(":checked");
}

function changeModalNameValue(name) {
    $('#modal-input-name').val(name);
}

function changeModalDescriptionValue(description) {
    $('#modal-textarea-description').val(description);
}

function setModalSaveButtonOnClick(func) {
    $('#modal-button-save').off().click(func);
}

function showModal(func) {
    $('#modal_general').off().on('show.bs.modal', func)
        .on('hidden.bs.modal', function () {
            clearElementsInModal();
        }).modal();
}

function hideModal() {
    $('#modal_general').modal('hide');
}

function allowClosingModal() {
    $('#modal_general .close').css('visibility', 'visible');
    $('#modal-button-close').show();
    // noinspection JSPotentiallyInvalidConstructorUsage,JSUnresolvedVariable
    $.fn.modal.prototype.constructor.Constructor.Default.backdrop = true;
    // noinspection JSPotentiallyInvalidConstructorUsage,JSUnresolvedVariable
    $.fn.modal.prototype.constructor.Constructor.Default.keyboard = true;
}

function preventClosingModal() {
    $('#modal_general .close').css('visibility', 'hidden');
    $('#modal-button-close').hide();
    // noinspection JSPotentiallyInvalidConstructorUsage,JSUnresolvedVariable
    $.fn.modal.prototype.constructor.Constructor.Default.backdrop = 'static';
    // noinspection JSPotentiallyInvalidConstructorUsage,JSUnresolvedVariable
    $.fn.modal.prototype.constructor.Constructor.Default.keyboard = false;
}

function clearErrorsInModal() {
    $('#modal-name-error').attr('hidden', 'true').html("");
    $('#modal-desc-error').attr('hidden', 'true').html("");
    $('#modal-condition-error').attr('hidden', 'true').html("");
}

function addConstraintErrorToModal(label, newError) {
    const errorText = label.html();

    label.removeAttr("hidden")
        .append(errorText === "" ? "" + newError : "<br>" + newError);
}

function showEditInfoButton() {
    $("#edit-game-info-button").show();
}

function hideEditInfoButton() {
    $("#edit-game-info-button").hide();
}

function showPlayButton() {
    $("#play-button").show();
}

function hidePlayButton() {
    $("#play-button").hide();
}

try {
    module.exports = {
        prepareModalForAddingRoomNode, prepareModalForAddingChoiceNode,
        prepareModalForAddingConditionNode, prepareModalForEditingRoomNode,
        prepareModalForEditingChoiceNode, prepareModalForEditingConditionNode,
        prepareModalForCreatingGame, prepareModalForUpdatingGameInfo,
        showEditInfoButton, hideEditInfoButton, showPlayButton, hidePlayButton,
        showModal, hideModal, preventClosingModal, setModalSaveButtonOnClick,
        getNameValueFromModal, getDescValueFromModal, isModalCheckboxChecked,
        getSelectValueFromModal, getModalRadioValue, clearErrorsInModal,
        showModalFieldErrors,
    }
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}