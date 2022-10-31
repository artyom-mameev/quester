function getNextChoices(room, activeFlags) {
    const choices = [];

    const potentialChoices = room.children;

    for (const potentialChoice of potentialChoices) {
        if (potentialChoice.type === NODE_TYPE.CHOICE) {
            const nextReachableRoom = getNextReachableRoom(potentialChoice,
                activeFlags);

            if (nextReachableRoom !== undefined) {
                choices.push({
                    text: potentialChoice.name, id: potentialChoice.id,
                    type: NODE_TYPE.CHOICE, nextRoom: nextReachableRoom
                });
            }
        }

        if (potentialChoice.type === NODE_TYPE.FLAG) {
            // if this is not an activated flag
            if (activeFlags.indexOf(potentialChoice.id) === -1) {
                choices.push({ /*make choice that reloads the same room
                                (some choice of the room may lead to another room
                                 after triggering of a condition*/
                    text: potentialChoice.name, id: potentialChoice.id,
                    type: NODE_TYPE.FLAG, nextRoom: room
                });
            }
        }
    }
    return choices;
}

try {
    module.exports = {
        getNextChoices
    };
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}