package com.artyommameev.quester.test;

import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.repository.GameRepository;
import com.artyommameev.quester.repository.UserRepository;
import lombok.val;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

/**
 * A component that saves a test entities into the database on
 * the application start.
 *
 * @author Artyom Mameev
 */
@Component
public class TestEntitiesProvider {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param userRepository a repository for querying and saving
     *                       {@link User} objects.
     * @param gameRepository a repository for querying and saving
     *                       {@link Game} objects.
     * @see UserRepository
     * @see GameRepository
     */
    public TestEntitiesProvider(UserRepository userRepository,
                                GameRepository gameRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    /**
     * Creates test entities on {@link ContextRefreshedEvent}.
     * <p>
     * Entities that are created:
     * <p>
     * Test admin {@link User}
     * <p>
     * Username: 'admin';<br>
     * E-mail: 'admin@test.com';<br>
     * Password: 'testpassword' (encrypted by {@link BCryptPasswordEncoder});<br>
     * Roles: 'ROLE_USER' and 'ROLE_ADMIN'.
     * <p>
     * Test normal {@link User}
     * <p>
     * Username: 'user';<br>
     * E-mail: 'user@test.com';<br>
     * Password: 'testpassword' (encrypted by {@link BCryptPasswordEncoder});<br>
     * Roles: 'ROLE_USER'.
     * <p>
     * Test {@link Game}
     * <p>
     * Name: 'Test Game';<br>
     * Description: 'Test Description';<br>
     * Language: 'English';<br>
     * User: Test admin user;<br>
     * Published: 'true'.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void createTestEntities() {
        try {
            val admin = new User("admin", "admin@test.com",
                    "testpassword", "testpassword",
                    new BCryptPasswordEncoder(), Arrays.asList("ROLE_USER",
                    "ROLE_ADMIN"));

            userRepository.save(admin);

            val user = new User("user", "user@test.com",
                    "testpassword", "testpassword",
                    new BCryptPasswordEncoder(), Collections.singletonList(
                    "ROLE_USER"));

            userRepository.save(user);

            val testGame = createTestGame(admin);
            val inTheMiddleOfNowhere = createInTheMiddleOfNowhere(user);
            val oceanRampage = createOceanRampage(user);
            val deadlyDesert = createDeadlyDesert(admin);

            gameRepository.saveAll(Arrays.asList(testGame, inTheMiddleOfNowhere,
                    oceanRampage, deadlyDesert));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Game createDeadlyDesert(User user) throws Exception {
        val deadlyDesert = new Game("Deadly Desert",
                "Can you get out of the middle of the deadly desert?",
                "English", user, true);

        deadlyDesert.addNode("id1", "###", "The Desert",
                "You find yourself in the middle of the desert, not" +
                        " a soul around. What will you do?",
                GameNode.NodeType.ROOM, null, null,
                user);
        deadlyDesert.addNode("id2", "id1",
                "Go towards the clouds", null,
                GameNode.NodeType.CHOICE, null, null,
                user);
        deadlyDesert.addNode("id3", "id2", "The Oasis",
                "You have reached the oasis and have quenched your" +
                        " thirst. Nearby you notice a bush with berries. " +
                        "On the horizon you see a cave.",
                GameNode.NodeType.ROOM, null, null,
                user);
        deadlyDesert.addNode("id4", "id3",
                "Pick berries from the bush", null,
                GameNode.NodeType.FLAG, null, null,
                user);
        deadlyDesert.addNode("id5", "id3",
                "Go towards the cave", null,
                GameNode.NodeType.CHOICE, null, null,
                user);
        deadlyDesert.addNode("id6", "id5",
                null, null, GameNode.NodeType.CONDITION,
                "id4", GameNode.Condition.FlagState.ACTIVE,
                user);
        deadlyDesert.addNode("id7", "id6", "Poisoned Death",
                "You got hungry and ate the berries picked from the" +
                        " bush, and then collapsed from fatigue and fell" +
                        " asleep soundly and never woke up, because the" +
                        " berries turned out to be poisonous.",
                GameNode.NodeType.ROOM, null, null,
                user);
        deadlyDesert.addNode("id8", "id5", "The Cave",
                "You were left hungry, but you started yourself a good " +
                        "place to stay for the night. To be continued...",
                GameNode.NodeType.ROOM, null, null,
                user);
        deadlyDesert.addNode("id9", "id1",
                "Go towards the cave", null,
                GameNode.NodeType.CHOICE, null, null,
                user);
        deadlyDesert.addNode("id10", "id9", "The Deep Desert",
                "The palm trees on the horizon were a mirage. " +
                        "You collapsed without strength, dying of thirst.",
                GameNode.NodeType.ROOM, null, null,
                user);

        return deadlyDesert;
    }

    private Game createOceanRampage(User user) throws Exception {
        val oceanRampage = new Game("Буйство океана",
                "Вы застряли на корабле посреди шторма. Сможете ли вы" +
                        " превозмочь буйство природы?", "Русский", user,
                true);

        oceanRampage.addNode("id1", "###", "Шторм",
                "Ваш корабль попал в шторм. Вы видите, как на волнах" +
                        " качается плот с человеком. Что вы будете делать?",
                GameNode.NodeType.ROOM, null, null,
                user);
        oceanRampage.addNode("id2", "id1",
                "Спасти человека", null,
                GameNode.NodeType.FLAG, null, null,
                user);
        oceanRampage.addNode("id3", "id1",
                "Переждать шторм в трюме", null,
                GameNode.NodeType.CHOICE, null, null,
                user);
        oceanRampage.addNode("id4", "id3", "Трюм",
                "Вы спустились в трюм и вдруг видите небольшую" +
                        " пробоину в стене.",
                GameNode.NodeType.ROOM, null, null,
                user);
        oceanRampage.addNode("id5", "id4",
                "Отыскать рабочего", null,
                GameNode.NodeType.CHOICE, null, null,
                user);
        oceanRampage.addNode("id6", "id5", "Каюта рабочего",
                "Рабочего одолела морская болезнь и он не в силах" +
                        " заниматься ремонтом судна. Тем временем пробоина" +
                        " начала расширяться, трюм начал заполняться водой и" +
                        " корабль начал идти ко дну.",
                GameNode.NodeType.ROOM, null, null,
                user);
        oceanRampage.addNode("id7", "id4",
                "Попытаться заделать пробоину своими силами", null,
                GameNode.NodeType.CHOICE, null, null,
                user);
        oceanRampage.addNode("id8", "id7",
                null, null, GameNode.NodeType.CONDITION,
                "id2", GameNode.Condition.FlagState.ACTIVE,
                user);
        oceanRampage.addNode("id9", "id8", "Возле пробоины",
                "Вы начали заделывать пробоину, но она начала" +
                        " расширяться. У вас не хватало сил, но тут вас пришёл" +
                        " на помощь ранее спасённый путник. Вместе вы быстро" +
                        " заделали пробоину. Продолжение следует...",
                GameNode.NodeType.ROOM, null, null,
                user);
        oceanRampage.addNode("id10", "id7", "Тонущий корабль",
                "Вы начали заделывать пробоину, но она начала" +
                        " расширяться. У вас не хватило сил, вас отбросило" +
                        " потоками воды и корабль пошёл ко дну.",
                GameNode.NodeType.ROOM, null, null,
                user);
        oceanRampage.addNode("id11", "id1",
                "Следить за ситуацией на палубе", null,
                GameNode.NodeType.CHOICE, null, null,
                user);
        oceanRampage.addNode("id12", "id11",
                null, null, GameNode.NodeType.CONDITION,
                "id2", GameNode.Condition.FlagState.ACTIVE,
                user);
        oceanRampage.addNode("id13", "id12", "Палуба",
                "Порывом ветра вас чуть не снесло за борт, но спасённый" +
                        " незнакомец вовремя протянул вам руку. " +
                        "Продолжение следует.",
                GameNode.NodeType.ROOM, null, null,
                user);
        oceanRampage.addNode("id14", "id11", "Ледяная вода",
                "Порывом ветра вас унесло за борт, вы утонули.",
                GameNode.NodeType.ROOM, null, null,
                user);

        return oceanRampage;
    }

    private Game createInTheMiddleOfNowhere(User user) throws Exception {
        val inTheMiddleOfNowhere = new Game("In The Middle of Nowhere",
                "You are caught in a wilderness, from which you need" +
                        " to get out alive.", "English", user,
                true);

        inTheMiddleOfNowhere.addNode("id1", "###", "Deep Forest",
                "You are in a deep forest. What will you do?",
                GameNode.NodeType.ROOM, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id2", "id1",
                "Walk Down The Path", null,
                GameNode.NodeType.CHOICE, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id3", "id1",
                "Take a Stick", null,
                GameNode.NodeType.FLAG, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id4", "id1",
                "Go to The Swamp", null,
                GameNode.NodeType.CHOICE, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id5", "id2",
                "Forester's Hut", "You followed the path and came" +
                        " upon the forester's hut. The hut has things you can" +
                        " take with you, and you also see a meadow nearby.",
                GameNode.NodeType.ROOM, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id6", "id5",
                "Take a Gun", null,
                GameNode.NodeType.FLAG, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id7", "id5",
                "Clean up The Hut", null,
                GameNode.NodeType.FLAG, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id8", "id5",
                "Go to The Meadow", null,
                GameNode.NodeType.CHOICE, null, null,
                user);

        inTheMiddleOfNowhere.addNode("id9", "id5",
                "Stay in The Hut", null,
                GameNode.NodeType.CHOICE, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id10", "id8",
                "The Meadow", "You have reached the meadow." +
                        " To be continued...",
                GameNode.NodeType.ROOM, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id11", "id9",
                null, null, GameNode.NodeType.CONDITION,
                "id6", GameNode.Condition.FlagState.NOT_ACTIVE,
                user);
        inTheMiddleOfNowhere.addNode("id12", "id11",
                null, null, GameNode.NodeType.CONDITION,
                "id7", GameNode.Condition.FlagState.ACTIVE,
                user);
        inTheMiddleOfNowhere.addNode("id13", "id12",
                "Dinner with The Forester", "The forester came" +
                        " and was happy that you cleaned his house and didn't" +
                        " do anything wrong. He invited you to dinner. To be" +
                        " continued...",
                GameNode.NodeType.ROOM, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id14", "id9",
                "Shot By The Forester", "The enraged forester," +
                        " seeing that you broke into his house, shot you.",
                GameNode.NodeType.ROOM, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id15", "id4",
                null, null, GameNode.NodeType.CONDITION,
                "id3", GameNode.Condition.FlagState.ACTIVE,
                user);
        inTheMiddleOfNowhere.addNode("id16", "id15",
                "Swamp Shack", "As you walk through the swamp, " +
                        "you almost get sucked into the swamp, but thanks to a" +
                        " stick you manage to get out. You stumble upon a swamp" +
                        " shack. To be continued...",
                GameNode.NodeType.ROOM, null, null,
                user);
        inTheMiddleOfNowhere.addNode("id17", "id4",
                "Drowned in The Swamps", "As you were walking" +
                        " through the swamp, you were suddenly sucked into the" +
                        " swamp. You couldn't get out.",
                GameNode.NodeType.ROOM, null, null,
                user);

        return inTheMiddleOfNowhere;
    }

    private Game createTestGame(User user) throws Exception {
        val testGame = new Game("Test Game", "Test Description",
                "English", user, true);

        testGame.addNode("id1", "###", "Test Room 1",
                "Test Room 1 Desc", GameNode.NodeType.ROOM,
                null, null, user);
        testGame.addNode("id2", "id1", "Test Flag 1",
                null, GameNode.NodeType.FLAG,
                null, null, user);
        testGame.addNode("id3", "id1", "Test Choice 1",
                null, GameNode.NodeType.CHOICE,
                null, null, user);
        testGame.addNode("id4", "id1", "Test Choice 2",
                null, GameNode.NodeType.CHOICE,
                null, null, user);
        testGame.addNode("id5", "id3", null,
                null, GameNode.NodeType.CONDITION,
                "id2", GameNode.Condition.FlagState.ACTIVE,
                user);
        testGame.addNode("id6", "id3", "Test Room 3",
                "Test Room 3 Desc", GameNode.NodeType.ROOM,
                null, null, user);
        testGame.addNode("id7", "id5", "Test Room 2",
                "Test Room 2 Desc", GameNode.NodeType.ROOM,
                null, null, user);
        testGame.addNode("id8", "id4", null,
                null, GameNode.NodeType.CONDITION,
                "id2", GameNode.Condition.FlagState.NOT_ACTIVE,
                user);
        testGame.addNode("id9", "id4", "Test Room 5",
                "Test Room 5 Desc", GameNode.NodeType.ROOM,
                null, null, user);
        testGame.addNode("id10", "id8", "Test Room 4",
                "Test Room 4 Desc", GameNode.NodeType.ROOM,
                null, null, user);

        return testGame;
    }
}
