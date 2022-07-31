////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                     Components                                                     //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// super class of all Components
// Components define all of an Entities behaviour
// this is really a Component & a System but I'll just call it Component from here on

World_Component : World_World {

	var <parent, <components; // every component has a parent which is an entity and quick access to its parent components

	initComponent{|argParent| parent = argParent; components = parent.components }

	freeComponent{
		this.free;
		parent = nil; components = nil; // << do i need to do this ? (prob good because it will show freeing errors)
	}

	free{} // for subclassing

	at{|component| ^components[component] } // shortcut

}


