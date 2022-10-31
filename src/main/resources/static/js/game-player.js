const FLAGS = [];

function goToNextRoom(room) {
    clearChoices();

    changeRoomTitle(room.name);
    changeRoomDesc(room.description);

    const choices = getNextChoices(room, FLAGS);

    if (choices.length === 0) {
        createGameOverChoices();
    }

    for (const choice of choices) {
        createGameChoice(choice, FLAGS);
    }
}

try {
    module.exports = {
        goToNextRoom
    };
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}