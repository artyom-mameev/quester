package com.artyommameev.quester.entity;

import com.artyommameev.quester.entity.gamenode.ConditionNode;
import com.artyommameev.quester.entity.gamenode.FlagNode;
import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.entity.gamenode.RoomNode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.sun.istack.Nullable;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OnDelete;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.*;

import static com.artyommameev.quester.QuesterApplication.*;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

/**
 * A Game domain entity. Encapsulates a text game that can be played.
 *
 * @author Artyom Mameev
 */
@Entity
@Table(name = "GAME")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public final class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    @Getter
    private long id;

    @Column(name = "NAME", nullable = false)
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE)
    @Getter
    private String name;

    @Column(name = "DESCRIPTION", nullable = false)
    @Size(min = MIN_STRING_SIZE, max = MAX_LONG_STRING_SIZE)
    @Getter
    private String description;

    @Column(name = "LANGUAGE", nullable = false)
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE)
    @Getter
    private String language;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ROOT_NODE_ID",
            referencedColumnName = "ID",
            foreignKey = @ForeignKey(name = "FK_GAME_ROOT_NODE_ID")
    )
    @Getter
    private RoomNode rootNode;

    @Column(name = "DATE")
    private Date date;

    @Column(name = "PUBLISHED", nullable = false)
    @Getter
    private boolean published;

    @Column(name = "RATING", insertable = false, updatable = false)
    @Getter
    //generated by trigger in the db
    private Double rating;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "USER_ID",
            referencedColumnName = "ID",
            updatable = false,
            foreignKey = @ForeignKey(name = "FK_GAME_USER_ID")
    )
    @Getter
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "game",
            fetch = FetchType.LAZY)
    //don't initialize the whole collection if just size() or contains() is called
    @LazyCollection(LazyCollectionOption.EXTRA)
    @OnDelete(action = CASCADE)
    private final List<Comment> comments = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "game",
            fetch = FetchType.LAZY)
    //don't initialize the whole collection if just size() or contains() is called
    @LazyCollection(LazyCollectionOption.EXTRA)
    @OnDelete(action = CASCADE)
    private final Set<Review> reviews = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "GAME_FAVORITED_BY_USER",
            joinColumns = @JoinColumn(name = "GAME_ID",
                    referencedColumnName = "ID",
                    foreignKey = @ForeignKey(name = "FK_USER_FAVORITE_GAME_ID")),
            inverseJoinColumns = @JoinColumn(name = "USER_ID",
                    referencedColumnName = "ID",
                    foreignKey = @ForeignKey(name = "FK_USER_FAVORITE_USER_ID")))
    //don't initialize the whole collection if just size() or contains() is called
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<User> favorited = new HashSet<>();

    /**
     * Instantiates a new Game.
     * <p>
     * If 'published' parameter is true, sets the Game date to the current date.
     * <p>
     * After the object is created, it must be saved in the database
     * to be assigned a unique id.
     *
     * @param name        a name of the game.
     * @param description a description of the game.
     * @param language    a language of the game.
     * @param user        an author of the game.
     * @param published   a status indicating whether the game is published
     *                    or not.
     * @throws EmptyStringException if any string argument is empty.
     * @throws NullValueException   if any argument is null.
     */
    public Game(String name, String description, String language,
                @NonNull User user, boolean published)
            throws EmptyStringException, NullValueException {
        setName(name);
        setDescription(description);
        setLanguage(language);
        this.user = user;
        this.published = published;

        if (published) {
            this.date = new Date();
        }
    }

    /**
     * Returns the date the game was published.
     * <p>
     * Each time returns a new {@link Date} object.
     *
     * @return the date the game was published if the game has already been
     * published, otherwise null.
     */
    public Date getDate() {
        if (date != null) {
            return new Date(date.getTime());
        } else return null;
    }

    /**
     * Returns all game {@link Comment}s.
     *
     * @return all game {@link Comment}s in a unmodifiable list.
     */
    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    /**
     * Returns the number of the game {@link Comment}s.
     *
     * @return the number of the game {@link Comment}s.
     */
    public int getCommentsCount() {
        return comments.size();
    }

    /**
     * Returns a specific {@link User}'s {@link Review} for the game.
     *
     * @param user the {@link User} whose review should be returned.
     * @return the {@link User}'s {@link Review} for the game if it was found,
     * otherwise null.
     */
    public Review getReviewFor(@NonNull User user) {
        return reviews.stream()
                .filter(r -> r.getUser().equals(user))
                .findFirst().orElse(null);
    }

    /**
     * Returns the game representation that can be sent as json.
     *
     * @return a {@link JsonReadyGame}
     * @throws NotPublishedException if the game is not published.
     */
    public JsonReadyGame getJsonReadyGame() throws NotPublishedException {
        if (!published) {
            throw new NotPublishedException("Cannot get json of" +
                    " not published game");
        }

        return new JsonReadyGame(this);
    }

    /**
     * Checks whether the game is in specific {@link User}'s favorites.
     *
     * @param user the {@link User} to check if the game is in their favorites.
     * @return true if the game is in the {@link User}'s favorites, otherwise
     * false.
     */
    public boolean isFavoritedBy(@NonNull User user) {
        return favorited.contains(user);
    }

    /**
     * Checks if the game can be viewed by a specific {@link User}.
     *
     * @param user the {@link User} to check if they can view the game.
     * @return true if the game is published or the {@link User} is not null and
     * can modify the game, otherwise false.
     */
    public boolean canBeViewedFrom(@Nullable User user) {
        /*if the game is published, it can be viewed from guest,
        if the game is not published, it can be viewed only from
        user who can modify the game*/
        return published || (user != null && canBeModifiedFrom(user));
    }

    /**
     * Checks if the game can be modified by a specific {@link User}.
     *
     * @param user the {@link User} to check if they can modify the game.
     * @return true, if the {@link User} is the author of the game or the admin,
     * otherwise false.
     */
    public boolean canBeModifiedFrom(@NonNull User user) {
        return this.user.equals(user) || user.isAdmin();
    }

    /**
     * Adds a {@link Comment} to the game.
     *
     * @param text the {@link Comment} text.
     * @param user the {@link Comment} author.
     * @throws NotPublishedException if the game is not published.
     * @throws EmptyStringException  if the {@link Comment} text is empty.
     * @throws NullValueException    if the {@link Comment} text is null.
     */
    public void addComment(String text, @NonNull User user)
            throws NotPublishedException, EmptyStringException,
            NullValueException {
        if (!published) {
            throw new NotPublishedException("Cannot add the comment," +
                    " game is not published");
        }

        try {
            comments.add(new Comment(this, user, text));
        } catch (Comment.EmptyTextException e) {
            throw new EmptyStringException(e);
        } catch (Comment.NullValueException e) {
            throw new NullValueException(e);
        }
    }

    /**
     * Edits an existing game {@link Comment}.
     *
     * @param commentId an id of the {@link Comment} that should be edited.
     * @param text      a new {@link Comment} text.
     * @param user      a {@link User} who tries to edit the {@link Comment}.
     * @throws NotPublishedException          if the game is not published.
     * @throws CommentNotFoundException       if the {@link Comment} is not found.
     * @throws ForbiddenManipulationException if the {@link User} cannot modify
     *                                        the {@link Comment}.
     * @throws EmptyStringException           if the {@link Comment} text is empty.
     * @throws NullValueException             if the {@link Comment} text is null.
     */
    public void editComment(long commentId, String text, @NonNull User user)
            throws NotPublishedException, CommentNotFoundException,
            ForbiddenManipulationException, EmptyStringException,
            NullValueException {
        if (!published) {
            throw new NotPublishedException("Cannot edit the comment," +
                    " the game is not published");
        }

        val comment = getComment(commentId);

        if (comment == null) {
            throw new CommentNotFoundException("Comment " + commentId +
                    " cannot be found");
        }

        try {
            comment.editText(text, user);
        } catch (Comment.ForbiddenManipulationException e) {
            throw new ForbiddenManipulationException(e);
        } catch (Comment.NullValueException e) {
            throw new NullValueException(e);
        } catch (Comment.EmptyTextException e) {
            throw new EmptyStringException(e);
        }

    }

    /**
     * Removes an existing game {@link Comment}.
     *
     * @param commentId an id of the {@link Comment} that should be removed.
     * @param user      a {@link User} who tries to remove the {@link Comment}.
     * @throws NotPublishedException          if the game is not published.
     * @throws CommentNotFoundException       if the {@link Comment} is not found.
     * @throws ForbiddenManipulationException if the {@link User} cannot modify
     *                                        the {@link Comment}.
     */
    public void deleteComment(long commentId, @NonNull User user)
            throws NotPublishedException, CommentNotFoundException,
            ForbiddenManipulationException {
        if (!published) {
            throw new NotPublishedException("Cannot delete the comment, " +
                    "the game is not published");
        }
        val comment = getComment(commentId);

        if (comment == null) {
            throw new CommentNotFoundException("Comment " + commentId +
                    " cannot be found");
        }
        if (!comment.canBeModifiedBy(user)) {
            throw new ForbiddenManipulationException("Comment " +
                    commentId + " cannot be modified by user " +
                    user.getUsername());
        }

        comments.remove(comment);
    }

    /**
     * Adds a {@link Review} to the game.
     *
     * @param rating a {@link Review.Rating} of the {@link Review}.
     * @param user   a {@link User} who rates the game.
     * @throws NotPublishedException if the game is not published.
     */
    public void addReview(Review.Rating rating, @NonNull User user)
            throws NotPublishedException {
        if (!published) {
            throw new NotPublishedException("Cannot add review to " +
                    "a non-published game");
        }

        reviews.add(new Review(rating, this, user));
    }

    /**
     * Edits an existing game {@link Review}.
     *
     * @param rating a new {@link Review.Rating} of the {@link Review}.
     * @param user   a {@link User} who tries to edit their
     *               {@link Review} for the game.
     * @throws NotPublishedException   if the game is not published.
     * @throws ReviewNotFoundException if the {@link Review} is not found.
     */
    public void editReview(Review.Rating rating, @NonNull User user)
            throws NotPublishedException, ReviewNotFoundException {
        if (!published) {
            throw new NotPublishedException("Cannot edit review of " +
                    "a non-published game");
        }

        val review = getReviewFor(user);

        if (review == null) {
            throw new ReviewNotFoundException("Review from user " +
                    user.getUsername() + " cannot be found");
        }

        review.updateRating(rating);
    }

    /**
     * Adds a {@link User} to the list of {@link User}s who favorited the game.
     *
     * @param user the {@link User} who tries to favorite the game.
     * @throws NotPublishedException if the game is not published.
     */
    public void addFavoritedUser(@NonNull User user)
            throws NotPublishedException {
        if (!published) {
            throw new NotPublishedException("The game is not published" +
                    " and cannot be added to favorites");
        }

        favorited.add(user);
    }

    /**
     * Removes a {@link User} from the list of users who favorited the game.
     *
     * @param user a {@link User} who tries to unfavorite the game.
     * @throws NotPublishedException if the game is not published.
     */
    public void removeFavoritedUser(@NonNull User user)
            throws NotPublishedException {
        if (!published) {
            throw new NotPublishedException("Game is not published " +
                    "and cannot be removed from favorites");
        }

        favorited.remove(user);
    }

    /**
     * Changes the name of the game.
     *
     * @param name a new name of the game.
     * @param user a {@link User} who tries to change the name of the game.
     * @throws ForbiddenManipulationException if the {@link User} is not allowed
     *                                        to change the name of the game.
     * @throws EmptyStringException           if the name is empty.
     * @throws NullValueException             if the name is null.
     */
    public void updateName(String name, @NonNull User user)
            throws ForbiddenManipulationException, EmptyStringException,
            NullValueException {
        if (!canBeModifiedFrom(user)) {
            throw new ForbiddenManipulationException("User " +
                    user.getUsername() + " cannot modify the game " + name +
                    " (" + id + ")");
        }

        setName(name);
    }

    /**
     * Changes the description of the game.
     *
     * @param description a new description of the game.
     * @param user        a {@link User} who tries to change the description
     *                    of the game.
     * @throws ForbiddenManipulationException if the {@link User} is not allowed
     *                                        to change the description of
     *                                        the game.
     * @throws EmptyStringException           if the description is empty.
     * @throws NullValueException             if the description is null.
     */
    public void updateDescription(String description, @NonNull User user)
            throws ForbiddenManipulationException, EmptyStringException,
            NullValueException {
        if (!canBeModifiedFrom(user)) {
            throw new ForbiddenManipulationException("User " +
                    user.getUsername() + " cannot modify the game " + name +
                    " (" + id + ")");
        }

        setDescription(description);
    }

    /**
     * Changes the language of the game.
     *
     * @param language a new language of the game.
     * @param user     a {@link User} who tries to change the language of
     *                 the game.
     * @throws ForbiddenManipulationException if the {@link User} is not allowed
     *                                        to change the language of
     *                                        the game.
     * @throws EmptyStringException           if the language is empty.
     * @throws NullValueException             if the language is null.
     */
    public void updateLanguage(String language, @NonNull User user)
            throws ForbiddenManipulationException, EmptyStringException,
            NullValueException {
        if (!canBeModifiedFrom(user)) {
            throw new ForbiddenManipulationException("User " +
                    user.getUsername() + " cannot modify the game " + name +
                    " (" + id + ")");
        }

        setLanguage(language);
    }

    /**
     * Changes the published status of the game.
     * <p>
     * If the game has not been published and its status changes
     * to published for the first time, the date of the game creation
     * sets to the current date.
     *
     * @param published a new published status of the game.
     * @param user      a {@link User} who tries to change the published status
     *                  of the game.
     * @throws ForbiddenManipulationException if the {@link User} is not allowed
     *                                        to change the published status of
     *                                        the game.
     */
    public void updatePublished(boolean published, @NonNull User user)
            throws ForbiddenManipulationException {
        if (!canBeModifiedFrom(user)) {
            throw new ForbiddenManipulationException("User " +
                    user.getUsername() + " cannot modify the game " + name +
                    " (" + id + ")");
        }

        /* if the game has not been published and user changes the game status
        to published, and if game's release date has not been set, set date to
        now */
        if ((!this.published & published) && date == null) {
            date = new Date();
        }

        this.published = published;
    }

    /**
     * Adds a new {@link GameNode} to the game.
     * <p>
     * If the game does not have {@link GameNode} yet, the added
     * {@link GameNode} is considered as root node. The root node must be a
     * {@link RoomNode} with '###' parent id.
     * <p>
     * Depending on the {@link GameNode} type, certain method parameters may not
     * be used and therefore may be omitted. See the documentation of specific
     * {@link GameNode} implementations to see constraints of adding particular
     * node types.
     *
     * @param id                 an id of the new {@link GameNode}.
     * @param parentId           an id of new {@link GameNode}'s parent.
     * @param name               a name of the new {@link GameNode}.
     * @param description        a description of the new {@link GameNode}.
     * @param type               a type of the new {@link GameNode}.
     * @param conditionFlagId    an id of a {@link FlagNode}, which should
     *                           trigger the new {@link GameNode} at a specific
     *                           {@link FlagNode} state (only if the new game
     *                           node is {@link ConditionNode}).
     * @param conditionFlagState a {@link FlagNode} state at which
     *                           the new {@link GameNode} should be triggered
     *                           (only only if the new game node is
     *                           ({@link ConditionNode}).
     * @param user               a {@link User} who tries to add the new
     *                           {@link GameNode} to the game.
     * @throws IllegalRootNodeTypeException   if trying to add a
     *                                        non-{@link RoomNode} as a root node.
     * @throws NotRootNodeException           if a root node parent type is not
     *                                        equal to '###'.
     * @throws ForbiddenManipulationException if the {@link User} is not allowed
     *                                        to add new {@link GameNode} to
     *                                        the game.
     * @throws NodeVerificationException      if a verification error occurs when
     *                                        creating the {@link GameNode}.
     */
    public void addNode(String id, String parentId, String name,
                        String description, GameNode.NodeType type,
                        String conditionFlagId, GameNode.Condition.FlagState
                                conditionFlagState, @NonNull User user)
            throws IllegalRootNodeTypeException,
            ForbiddenManipulationException, NodeVerificationException,
            NotRootNodeException {
        if (!canBeModifiedFrom(user)) {
            throw new ForbiddenManipulationException("User " +
                    user.getUsername() + " cannot modify the game " + name +
                    " (" + id + ")");
        }

        if (rootNode != null) {
            try {
                rootNode.addNodeToChildren(id, parentId, name, description, type,
                        conditionFlagId, conditionFlagState);
            } catch (GameNode.FlagNotExistsException |
                    GameNode.ParentNotExistsException |
                    GameNode.ParentMismatchException |
                    GameNode.EmptyStringException |
                    GameNode.AlreadyExistsException |
                    GameNode.NullValueException e) {
                throw new NodeVerificationException(e);
            }
        } else {
            if (!parentId.equals("###")) {
                throw new NotRootNodeException("The game does not have" +
                        " a root node! If this node is a root node," +
                        " the node's parent id should be '###'");
            }
            if (!type.equals(GameNode.NodeType.ROOM)) {
                throw new IllegalRootNodeTypeException("Root node" +
                        " type can only be a 'ROOM'");
            }

            try {
                rootNode = new RoomNode(id, name, description);
            } catch (GameNode.EmptyStringException |
                    GameNode.NullValueException e) {
                throw new NodeVerificationException(e);
            }
        }
    }

    /**
     * Edits an existing {@link GameNode}.
     * <p>
     * Depending on the {@link GameNode} type, certain method parameters may not
     * be used and therefore may be omitted. See the documentation of specific
     * {@link GameNode} implementations to see constraints of adding particular
     * node types.
     *
     * @param nodeId             an id of the {@link GameNode} that should be
     *                           edited.
     * @param name               a new name of the {@link GameNode}.
     * @param description        a new description of the {@link GameNode}.
     * @param conditionFlagId    a new id of a {@link FlagNode}, which
     *                           should trigger the {@link GameNode}
     *                           at a specific {@link FlagNode} state
     *                           (only if the game node type is
     *                           {@link ConditionNode}).
     * @param conditionFlagState a new {@link FlagNode} state at which
     *                           the {@link GameNode} should be triggered
     *                           (only if the game node type is
     *                           {@link ConditionNode}).
     * @param user               a {@link User} who tries to edit
     *                           the {@link GameNode}.
     * @throws ForbiddenManipulationException if the {@link User} is not allowed
     *                                        to edit a {@link GameNode} for
     *                                        the game.
     * @throws NodeVerificationException      if a verification error occurs
     *                                        when editing the {@link GameNode}
     * @throws RootNodeNotExistsException     if the root {@link GameNode} does
     *                                        not yet exist.
     * @throws NodeNotFoundException          if the {@link GameNode} is not
     *                                        found.
     */
    public void editNode(@NonNull String nodeId, String name, String description,
                         String conditionFlagId, GameNode.Condition.FlagState
                                 conditionFlagState, @NonNull User user)
            throws ForbiddenManipulationException, NodeVerificationException,
            RootNodeNotExistsException, NodeNotFoundException {
        if (!canBeModifiedFrom(user)) {
            throw new ForbiddenManipulationException("User " +
                    user.getUsername() + " cannot modify the game " + name +
                    " (" + id + ")");
        }
        if (rootNode == null) {
            throw new RootNodeNotExistsException("Root node does not" +
                    " exist, nothing to edit");
        }

        try {
            rootNode.editNode(nodeId, name, description, conditionFlagId,
                    conditionFlagState);
        } catch (GameNode.FlagNotExistsException |
                GameNode.EmptyStringException | GameNode.NullValueException e) {
            throw new NodeVerificationException(e);
        } catch (GameNode.NodeNotFoundException e) {
            throw new NodeNotFoundException(e);
        }
    }

    /**
     * Removes an existing {@link GameNode}.
     *
     * @param nodeId an id of the {@link GameNode} that should be removed.
     * @param user   a {@link User} that tries to remove the {@link GameNode}.
     * @throws ForbiddenManipulationException if the {@link User} is not allowed
     *                                        to remove a {@link GameNode} for
     *                                        the game.
     * @throws RootNodeNotExistsException     if the root {@link GameNode} does
     *                                        not yet exist.
     * @throws NodeNotFoundException          if the {@link GameNode} is
     *                                        not found.
     */
    public void deleteNode(String nodeId, @NonNull User user)
            throws ForbiddenManipulationException, RootNodeNotExistsException,
            NodeNotFoundException, NodeVerificationException {
        if (!canBeModifiedFrom(user)) {
            throw new ForbiddenManipulationException("User " +
                    user.getUsername() + " cannot modify the game " + name +
                    " (" + id + ")");
        }
        if (rootNode == null) {
            throw new RootNodeNotExistsException("Root node does not" +
                    " exist, nothing to edit");
        }

        try {
            rootNode.deleteChildNode(nodeId);
        } catch (GameNode.NodeNotFoundException e) {
            throw new NodeNotFoundException(e);
        } catch (GameNode.RootNodeDeletingException e) {
            throw new ForbiddenManipulationException(e);
        } catch (GameNode.NullValueException e) {
            throw new NodeVerificationException(e);
        }
    }

    /**
     * Checks if two Game objects are equal to each other.
     * <p>
     * Objects are equal if:
     * <p>
     * - None of the objects are null and their types are the same;<br>
     * - Their database ids is equal to each other.
     *
     * @param o the object to be checked.
     * @return true if objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Game game = (Game) o;

        return id == game.id;
    }

    /**
     * Generates hash code for the object based on database id.
     *
     * @return the hash code for the object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns String representation of the object.
     *
     * @return the hash code for the object in the format:
     * "Game 'name' (id) from user 'user'"
     */
    @Override
    public String toString() {
        return "Game '" + name + "' (" + id + ") from user '" +
                user.getUsername() + "'";
    }

    private void setName(String name) throws EmptyStringException,
            NullValueException {
        if (name == null) {
            throw new NullValueException("Name cannot be null");
        }
        if (name.trim().isEmpty()) {
            throw new EmptyStringException("Name cannot be empty");
        }

        this.name = name.trim();
    }


    private void setDescription(String description) throws EmptyStringException,
            NullValueException {
        if (description == null) {
            throw new NullValueException("Description cannot be null");
        }
        if (description.trim().isEmpty()) {
            throw new EmptyStringException("Description cannot be empty");
        }

        this.description = description.trim();
    }

    private void setLanguage(String language) throws EmptyStringException,
            NullValueException {
        if (language == null) {
            throw new NullValueException("Language cannot be null");
        }
        if (language.trim().isEmpty()) {
            throw new EmptyStringException("Language cannot be empty");
        }

        this.language = language.trim();
    }

    private Comment getComment(long commentId) {
        return comments.stream()
                .filter(c -> c.getId() == commentId)
                .findFirst().orElse(null);
    }

    /**
     * The simple data object that represents the game that can be sent
     * to third-party clients via json. Does not contain any private information
     * like user details.
     *
     * @author Artyom Mameev
     */
    @Data
    @AllArgsConstructor
    public static final class JsonReadyGame {
        private long id;
        private String name;
        private String description;
        private String language;
        private RoomNode rootNode;
        private String user;
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
        private Date date;
        private double rating;

        /**
         * Instantiates a new Json Ready Game.
         *
         * @param game the {@link Game} that needs to be converted into json
         */
        public JsonReadyGame(@NonNull Game game) {
            this.id = game.id;
            this.name = game.name;
            this.description = game.description;
            this.language = game.language;
            this.rootNode = game.rootNode;
            this.user = game.user.getUsername();
            this.date = game.date;
            this.rating = game.rating;
        }
    }

    /**
     * An exception indicating that some sort of forbidden manipulation
     * attempt has been made to a {@link Game}.
     */
    public static class ForbiddenManipulationException extends Exception {
        /**
         * Instantiates a new Forbidden Manipulation Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public ForbiddenManipulationException(String s) {
            super(s);
        }

        /**
         * Instantiates a new Forbidden Manipulation Exception.
         *
         * @param t the cause of the exception.
         */
        public ForbiddenManipulationException(Throwable t) {
            super(t);
        }
    }

    /**
     * An exception indicating that a certain {@link GameNode} is not found
     * in a {@link Game}.
     */
    public static class NodeNotFoundException extends Exception {
        /**
         * Instantiates a new Node Not Found Exception.
         *
         * @param t the cause of the exception.
         */
        public NodeNotFoundException(Throwable t) {
            super(t);
        }
    }

    /**
     * An exception indicating that a certain {@link Game}'s {@link Comment}
     * is not found.
     */
    public static class CommentNotFoundException extends Exception {
        /**
         * Instantiates a new Comment Not Found Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public CommentNotFoundException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a certain {@link Game}'s {@link Review}
     * is not found.
     */
    public static class ReviewNotFoundException extends Exception {
        /**
         * Instantiates a new Review Not Found Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public ReviewNotFoundException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a certain {@link Game} is not published.
     */
    public static class NotPublishedException extends Exception {
        /**
         * Instantiates a new Not Published Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public NotPublishedException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a root {@link GameNode} type is incorrect.
     */
    public static class IllegalRootNodeTypeException extends Exception {
        /**
         * Instantiates a new Illegal Root Node Type exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public IllegalRootNodeTypeException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that some verification issues were found
     * while processing a certain {@link GameNode}.
     */
    public static class NodeVerificationException extends Exception {
        /**
         * Instantiates a new Node Verification Exception.
         *
         * @param t the cause of the exception.
         */
        public NodeVerificationException(Throwable t) {
            super(t);
        }
    }

    /**
     * An exception indicating that a {@link GameNode} that meant to be root
     * node is actually not root node.
     */
    public static class NotRootNodeException extends Exception {
        /**
         * Instantiates a new Not Root Node Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public NotRootNodeException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that the game does not have a root
     * {@link GameNode}.
     */
    public static class RootNodeNotExistsException extends Exception {
        /**
         * Instantiates a new Root Node Not Exists Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public RootNodeNotExistsException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that certain {@link Game} string parameter
     * is empty.
     */
    public static class EmptyStringException extends Exception {
        /**
         * Instantiates a new Empty String Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public EmptyStringException(String s) {
            super(s);
        }

        /**
         * Instantiates a new Empty String Exception.
         *
         * @param t the cause of the exception.
         */
        public EmptyStringException(Throwable t) {
            super(t);
        }
    }

    /**
     * An exception indicating that a certain {@link Game} parameter is null.
     */
    public static class NullValueException extends Exception {
        /**
         * Instantiates a new Null Value Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public NullValueException(String s) {
            super(s);
        }

        /**
         * Instantiates a new Null Value Exception.
         *
         * @param t the cause of the exception.
         */
        public NullValueException(Throwable t) {
            super(t);
        }
    }
}
