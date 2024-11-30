package filesetup;

import objects.FoodItem;
import java.util.List;
public interface IFoodItemRepository {
    public List<FoodItem> getFoodItemsByCategory(int categoryId);
    public FoodItem getFoodItemById(int id);
}
