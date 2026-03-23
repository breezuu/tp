package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's photo in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhoto(String)}
 */
public class Photo {

    public static final String MESSAGE_CONSTRAINTS = "Photos should only be in .png, .jpg, .jpeg format";
    public static final String DEFAULT_PHOTO_PATH = "/images/pepe-default.png";
    /**
     * Path to photo must end with the extension any of the extensions .png, .jpg or .jpeg
     */
    public static final String VALIDATION_REGEX = "(?i)^.*\\.(png|jpg|jpeg)$";
    public final String value;


    /**
     * Constructs an {@code Photo}.
     *
     * @param path A valid file path to a photo.
     */
    public Photo(String path) {
        requireNonNull(path);

        // Check if provided file exists
        if (path.trim().isEmpty() || path.equals(DEFAULT_PHOTO_PATH)) {
            this.value = DEFAULT_PHOTO_PATH;
            return;
        }

        // Check Extension is .png, .jpg or .jpeg
        checkArgument(isValidPhoto(path), MESSAGE_CONSTRAINTS);
        this.value = path;
    }

    /**
     * Returns true if a given string is a valid file path and the image is of the format .png, .jpg, .jpeg.
     */
    public static boolean isValidPhoto(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if the photo is the default placeholder.
     */
    public boolean isDefault() {
        return this.value.equals(DEFAULT_PHOTO_PATH);
    }

    /**
     * Returns true if the photo is already in the directory.
     */
    public boolean isSavedLocally() {
        return this.value.startsWith("data/images/");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Photo)) {
            return false;
        }

        Photo otherPhoto = (Photo) other;
        return value.equals(otherPhoto.value);
    }

    @Override
    public String toString() {
        return this.value;
    }
}
